package com.example.design_pattern_verifier.VisitorPattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

@SpringBootTest
public class DoubleDispatchTest {

    private JavaParser javaParser;

    @BeforeEach
    public void setup() {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedSolver));
        this.javaParser = new JavaParser(parserConfiguration);
    }

    public Map<String, Set<String>> extractElementToVisitorMappings(String resourcePath) throws Exception {
        Resource resource = new ClassPathResource(resourcePath);
        Path directoryPath = Paths.get(resource.getURI());
        Path combinedFilePath = this.combineJavaFiles(directoryPath);

        Map<String, Set<String>> elementToVisitorMappings = new HashMap<>();

        try (InputStream fileInputStream = Files.newInputStream(combinedFilePath)) {
            CompilationUnit cu = this.javaParser.parse(fileInputStream).getResult().get();
            
            MethodCallCollector methodCallCollector = new MethodCallCollector();
            cu.accept(methodCallCollector, null);
            methodCallCollector.finalizeMaps();
            Map<String, Set<String>> candidates = methodCallCollector.getCandidates();

            ClassHierarchyExtractor classHierarchyExtractor = new ClassHierarchyExtractor();
            cu.accept(classHierarchyExtractor, null);
            Map<String, String> subclassToSuperclassMap = classHierarchyExtractor.getSubclassToSuperclassMap();
            
            DoubleDispatchDetector doubleDispatchDetector = new DoubleDispatchDetector(candidates, subclassToSuperclassMap);
            cu.accept(doubleDispatchDetector, null);
            
            elementToVisitorMappings.putAll(doubleDispatchDetector.getElementToVisitorMappings());
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Files.deleteIfExists(combinedFilePath); 
        }

        return elementToVisitorMappings;
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

    @Test
    public void DoubleDispatch_vsr1() throws Exception {
        Map<String, Set<String>> result = this.extractElementToVisitorMappings("static/VisitorTestDirs/vsrc1");
        assertTrue(result.containsKey("Visitor") && result.get("Visitor").containsAll(Set.of("Clothing", "Book")));
        assertTrue(result.containsKey("PriceVisitor") && result.get("PriceVisitor").containsAll(Set.of("Clothing", "Book")));
    }

    @Test
    public void DoubleDispatch_vsr2() throws Exception {
        Map<String, Set<String>> result = this.extractElementToVisitorMappings("static/VisitorTestDirs/vsrc2");
        assertTrue(result.containsKey("ConcreteVisitor") && result.get("ConcreteVisitor").containsAll(Set.of("ElementB", "ElementA")));
        assertTrue(result.containsKey("IVisitor") && result.get("IVisitor").containsAll(Set.of("ElementB", "ElementA")));
    }

    @Test
    public void DoubleDispatch_vsr3() throws Exception {
        Map<String, Set<String>> result = this.extractElementToVisitorMappings("static/VisitorTestDirs/vsrc3");
        assertTrue(result.containsKey("ConcreteVisitor") && result.get("ConcreteVisitor").containsAll(Set.of("ElementB", "ElementA")));
        assertTrue(result.containsKey("IVisitor") && result.get("IVisitor").containsAll(Set.of("ElementB", "ElementA")));
    }

    @Test
    public void DoubleDispatch_vsr4() throws Exception {
        Map<String, Set<String>> result = this.extractElementToVisitorMappings("static/VisitorTestDirs/vsrc4");
        assertTrue(result.containsKey("B") && result.get("B").containsAll(Set.of("A1")));
        assertTrue(result.containsKey("B1") && result.get("B1").containsAll(Set.of("A1")));
    }

    @Test
    public void DoubleDispatch_vsr5() throws Exception {
        Map<String, Set<String>> result = this.extractElementToVisitorMappings("static/VisitorTestDirs/vsrc5");
        assertTrue(result.containsKey("ConcreteVisitor") && result.get("ConcreteVisitor").containsAll(Set.of("ElementB", "ElementA")));
        assertTrue(result.containsKey("IVisitor") && result.get("IVisitor").containsAll(Set.of("ElementB", "ElementA")));
    }

    @Test
    public void DoubleDispatch_vsr6() throws Exception {
        Map<String, Set<String>> result = this.extractElementToVisitorMappings("static/VisitorTestDirs/vsrc6");
        assertTrue(result.containsKey("ConcreteVisitor") && result.get("ConcreteVisitor").containsAll(Set.of("ElementB", "ElementA")));
        assertTrue(result.containsKey("IVisitor") && result.get("IVisitor").containsAll(Set.of("ElementB", "ElementA")));
    }
}