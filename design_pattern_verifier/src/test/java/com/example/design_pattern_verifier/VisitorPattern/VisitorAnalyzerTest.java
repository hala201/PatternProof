package com.example.design_pattern_verifier.VisitorPattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.example.design_pattern_verifier.service.VisitorPattern.ClassHierarchyExtractor;
import com.example.design_pattern_verifier.service.VisitorPattern.DoubleDispatchDetector;
import com.example.design_pattern_verifier.service.VisitorPattern.MethodCallCollector;
import com.example.design_pattern_verifier.service.VisitorPattern.MethodInformationExtractor;
import com.example.design_pattern_verifier.service.VisitorPattern.VisitorAnalyzer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

@SpringBootTest
public class VisitorAnalyzerTest {

    private JavaParser javaParser;

    @BeforeEach
    public void setup() {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedSolver));
        this.javaParser = new JavaParser(parserConfiguration);
    }

    private String analyzeVisitorPattern(String resourcePath) throws Exception {
        Resource resource = new ClassPathResource(resourcePath);
        Path directoryPath = Paths.get(resource.getURI());
        Path combinedFilePath = this.combineJavaFiles(directoryPath);

        MethodCallCollector methodCallCollector = new MethodCallCollector();
        ClassHierarchyExtractor classHierarchyExtractor = new ClassHierarchyExtractor();
        MethodInformationExtractor methodInformationExtractor = new MethodInformationExtractor();

        ParseResult<CompilationUnit> parseResult;
        try (InputStream fileInputStream = Files.newInputStream(combinedFilePath)) {
            parseResult = this.javaParser.parse(fileInputStream);
        } finally {
            Files.deleteIfExists(combinedFilePath);
        }

        Set<CompilationUnit> compilationUnits = new HashSet<>();
        parseResult.getResult().ifPresent(compilationUnits::add);

        compilationUnits.forEach(cu -> {
            cu.accept(classHierarchyExtractor, null);
            cu.accept(methodCallCollector, null);
            cu.accept(methodInformationExtractor, null);
        });
        methodCallCollector.finalizeMaps();

        DoubleDispatchDetector doubleDispatchDetector = new DoubleDispatchDetector(methodCallCollector.getCandidates(), classHierarchyExtractor.getSubclassToSuperclassMap());
        compilationUnits.forEach(cu -> cu.accept(doubleDispatchDetector, null));

        VisitorAnalyzer visitorAnalyzer = new VisitorAnalyzer(methodCallCollector.getCandidates(), doubleDispatchDetector.getElementToVisitorMappings(), classHierarchyExtractor.getSubclassToSuperclassMap(), methodInformationExtractor.getMethodInformation(), methodInformationExtractor.getInteractions());
        visitorAnalyzer.analyze();

        return visitorAnalyzer.getFormattedAnalysisResults();
    }

    private Path combineJavaFiles(Path directoryPath) throws Exception {
        Path combinedFilePath = Files.createTempFile("combined", ".java");
        Set<String> imports = new HashSet<>();
        StringBuilder combinedClasses = new StringBuilder();

        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        Files.lines(path).forEach(line -> {
                            if (line.startsWith("package ")) {
                            } else if (line.startsWith("import ")) {
                                imports.add(line);
                            } else {
                                combinedClasses.append(line).append("\n");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        }

        Files.write(combinedFilePath, (String.join("\n", imports) + "\n\n" + combinedClasses.toString()).getBytes(), StandardOpenOption.WRITE);
        return combinedFilePath;
    }

    private String normalizeString(String input) {
        return input.trim() 
                     .replaceAll("\r\n", "\n") 
                     .replaceAll("\n+", "\n") 
                     .replaceAll("[ \t]+", " ");
    }
    
    @Test
    public void testVisitorPatternAnalysis_vsrc1() throws Exception {
        String expected = this.normalizeString("""
        For Visitor:
            - detects double dispatch with: [Clothing, Book]
            - uses Overloaded Methods.
            - should handle all of [Clothing, Book]
            - does not have a body.
        Suggestions for Visitor:
        For PriceVisitor:
            - detects double dispatch with: [Clothing, Book]
            - uses Overloaded Methods.
            - should handle all of [Clothing, Book]
            - adequately handles all element types.
        Suggestions for PriceVisitor:
                """);
        String actual = this.normalizeString(this.analyzeVisitorPattern("static/VisitorTestDirs/vsrc1"));
        assertEquals(expected, actual);
    }

    @Test
    public void testVisitorPatternAnalysis_vsrc2() throws Exception {
        String expected = this.normalizeString("""
        For IVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Overloaded Methods.
            - should handle all of [ElementB, ElementA]
            - does not have a body.
        Suggestions for IVisitor:
        For ConcreteVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Overloaded Methods.
            - should handle all of [ElementB, ElementA]
            - adequately handles all element types.
        Suggestions for ConcreteVisitor:
                """);
        String actual = this.normalizeString(this.analyzeVisitorPattern("static/VisitorTestDirs/vsrc2"));
        assertEquals(expected, actual);
    }

    @Test
    public void testVisitorPatternAnalysis_vsrc3() throws Exception {
        String expected = this.normalizeString("""
        For IVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Overloaded Methods.
            - should handle all of [ElementB, ElementA]
            - does not have a body.
        Suggestions for IVisitor:
        For ConcreteVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Single Method Visitor.
            - should handle all of [ElementB, ElementA]
            - adequately handles all expected element types with a single method.
        Suggestions for ConcreteVisitor:
        - It is currently using a Single Method Visitor strategy, which might limit the flexibility and extensibility of your visitor pattern implementation.
        - Consider refactoring to include a specialized visit method for ElementB to improve clarity and maintainability.
        - Consider refactoring to include a specialized visit method for ElementA to improve clarity and maintainability.
                """);
        String actual = this.normalizeString(this.analyzeVisitorPattern("static/VisitorTestDirs/vsrc3"));
        assertEquals(expected, actual);
    }

    @Test
    public void testVisitorPatternAnalysis_vsrc4() throws Exception {
        String expected = this.normalizeString("""
        For B:
            - detects double dispatch with: [A1]
            - uses Overloaded Methods.
            - should handle all of [A1]
            - does not have a body.
        Suggestions for B:
        For B1:
            - detects double dispatch with: [A1]
            - uses Single Method Visitor.
            - should handle all of [A1]
            - adequately handles all expected element types with a single method.
        Suggestions for B1:
        - It is currently using a Single Method Visitor strategy, which might limit the flexibility and extensibility of your visitor pattern implementation.
        - Consider refactoring to include a specialized visit method for A1 to improve clarity and maintainability.
                """);
        String actual = this.normalizeString(this.analyzeVisitorPattern("static/VisitorTestDirs/vsrc4"));
        assertEquals(expected, actual);
    }

    @Test
    public void testVisitorPatternAnalysis_vsrc5() throws Exception {
        String expected = this.normalizeString("""
        For IVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Overloaded Methods.
            - should handle all of [ElementB, ElementA]
            - does not have a body.
        Suggestions for IVisitor:
        For ConcreteVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Single Method Visitor.
            - should handle all of [ElementB, ElementA]
            - does not adequately handle all element types. It doesn't interact with: [ElementB]
        Suggestions for ConcreteVisitor:
        - It does not interact with or handle the following class(es): [ElementB]
        Consider adding or refining visit methods to handle these element types explicitly.
        - It is currently using a Single Method Visitor strategy, which might limit the flexibility and extensibility of your visitor pattern implementation.
        - Consider refactoring to include a specialized visit method for ElementB to improve clarity and maintainability.
        - Consider refactoring to include a specialized visit method for ElementA to improve clarity and maintainability.
                """);
        String actual = this.normalizeString(this.analyzeVisitorPattern("static/VisitorTestDirs/vsrc5"));
        assertEquals(expected, actual);
    }

    @Test
    public void testVisitorPatternAnalysis_vsrc6() throws Exception {
        String expected = this.normalizeString("""
        For IVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Overloaded Methods.
            - should handle all of [ElementC, ElementB, ElementA]
            - does not have a body.
        Suggestions for IVisitor:
        For ConcreteVisitor:
            - detects double dispatch with: [ElementB, ElementA]
            - uses Overloaded Methods.
            - should handle all of [ElementC, ElementB, ElementA]
            - does not adequately handle all element types. It doesn't interact with: [ElementC]
        Suggestions for ConcreteVisitor:
        - It does not interact with or handle the following class(es): [ElementC]
        Consider adding or refining visit methods to handle these element types explicitly.
                """);
        String actual = this.normalizeString(this.analyzeVisitorPattern("static/VisitorTestDirs/vsrc6"));
        assertEquals(expected, actual);
    }
}
