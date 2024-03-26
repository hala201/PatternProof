package com.example.design_pattern_verifier.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.example.design_pattern_verifier.service.VisitorPattern.ControlFlowAnalyzer;
import org.springframework.stereotype.Service;

import com.example.design_pattern_verifier.service.VisitorPattern.ClassHierarchyExtractor;
import com.example.design_pattern_verifier.service.VisitorPattern.DoubleDispatchAnalyzer;
import com.example.design_pattern_verifier.service.VisitorPattern.MethodCallCollector;
import com.example.design_pattern_verifier.service.VisitorPattern.MethodInformationExtractor;
import com.example.design_pattern_verifier.service.VisitorPattern.MethodVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

@Service
public class AnalyzeService {
    private static final String UPLOADS_PATH = "uploads";

    public void analyseSourceDirectory(String directoryPath) {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(Paths.get(UPLOADS_PATH))
        );

        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedSolver));

        JavaParser javaParser = new JavaParser(parserConfiguration);

        try {
            Path combinedFilePath = this.combineJavaFiles(Paths.get(directoryPath));

            ParseResult<CompilationUnit> parseResult = javaParser.parse(combinedFilePath);
            List<CompilationUnit> compilationUnits = new ArrayList<>();
            parseResult.getResult().ifPresent(compilationUnits::add);
            
            this.processForVisitorPattern(compilationUnits, combinedSolver);
            // TBD: call the 3 main analyzers here, then ccombine/normalize outputs

            Files.deleteIfExists(combinedFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processForVisitorPattern(List<CompilationUnit> compilationUnits, CombinedTypeSolver combinedSolver) {
        MethodCallCollector methodCallCollector = new MethodCallCollector();
        ClassHierarchyExtractor classHierarchyExtractor = new ClassHierarchyExtractor();
        MethodInformationExtractor methodInformationExtractor = new MethodInformationExtractor();
        MethodVisitor methodVis = new MethodVisitor();

        compilationUnits.forEach(cu -> {
            cu.accept(classHierarchyExtractor, null);
            cu.accept(methodCallCollector, null);
        });
        methodCallCollector.finalizeMaps();

        compilationUnits.forEach(cu -> cu.accept(methodInformationExtractor, null));

        Map<String, Set<String>> methodInfo = methodInformationExtractor.getMethodInformation();
        
        this.logDoubleDispatch(methodInfo, methodCallCollector);

        DoubleDispatchAnalyzer DDanalyzer = new DoubleDispatchAnalyzer(
                methodInfo,
                methodCallCollector.getPotentialVisitors(),
                methodCallCollector.getPotentialElements(),
                methodCallCollector.getMethodCalls(),
                classHierarchyExtractor.getSubclassToSuperclassMap()
        );
        DDanalyzer.analyze();

        compilationUnits.forEach(cu -> cu.accept(methodVis, null));
        ControlFlowAnalyzer CFanalyzer = new ControlFlowAnalyzer(methodVis.getUsedTypes(), DDanalyzer.getVisType());
        CFanalyzer.analyze();
    }

    private void logDoubleDispatch(Map<String, Set<String>> methodInfo, MethodCallCollector methodCallCollector) {
        // TBD: remove these logs
        System.out.println("\nMethod Information:");
        methodInfo.forEach((className, methods) -> {
            System.out.println("Class: " + className);
            methods.forEach(method -> System.out.println("  Method: " + method));
        });

        System.out.println("\nPotential Visitors and Elements:");
        methodCallCollector.getPotentialVisitors().forEach((k, v) -> System.out.println("Visitor: " + k + " -> " + v));
        methodCallCollector.getPotentialElements().forEach((k, v) -> System.out.println("Element: " + k + " -> " + v));
        methodCallCollector.getMethodCalls().forEach((k, v) -> System.out.println("Method: " + k + " -> " + v));
        //
    }

    private Path combineJavaFiles(Path directoryPath) throws IOException {
        Path combinedFilePath = Files.createTempFile(directoryPath, "combined", ".java");
        Set<String> imports = new HashSet<>();
        StringBuilder combinedClasses = new StringBuilder();

        try (Stream<Path> paths = Files.walk(directoryPath)) {
            paths.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (String line : lines) {
                            if (line.startsWith("package ")) {
                                continue; 
                            } else if (line.startsWith("import ")) {
                                imports.add(line); 
                            } else {
                                combinedClasses.append(line).append("\n"); 
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }

        Files.write(combinedFilePath, String.join("\n", imports).getBytes(), StandardOpenOption.APPEND);
        Files.write(combinedFilePath, "\n".getBytes(), StandardOpenOption.APPEND);
        Files.write(combinedFilePath, combinedClasses.toString().getBytes(), StandardOpenOption.APPEND);

        return combinedFilePath;
    }
}
