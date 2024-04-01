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


import org.springframework.stereotype.Service;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.ChainExtractor;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.HandlerChainAnalyzer;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.RequestPropagationAnalyzer;
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
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

@Service
public class AnalyzeService {
    private static final String UPLOADS_PATH = "uploads";

    public String analyseSourceDirectory(String directoryPath, String pattern) {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(Paths.get(UPLOADS_PATH))
        );

        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(combinedSolver));

        JavaParser javaParser = new JavaParser(parserConfiguration);
        String result = "No results found.";
        try {
            Path combinedFilePath = this.combineJavaFiles(Paths.get(directoryPath));

            ParseResult<CompilationUnit> parseResult = javaParser.parse(combinedFilePath);
            List<CompilationUnit> compilationUnits = new ArrayList<>();
            parseResult.getResult().ifPresent(compilationUnits::add);
            
            switch (pattern) {
                case "visitor":
                    String visitorResult = this.processForVisitorPattern(compilationUnits, combinedSolver);
                    result = visitorResult.isEmpty() ? "No results found." : visitorResult;
                    break;
                case "chain":
                    String chainResult = this.processForChainOfResponsibility(compilationUnits);
                    result = chainResult.isEmpty() ? "No results found." : chainResult;
                    break;
                case "observer":
                    result = "not implemented yet";
                    break;
                default:
                    break;
            }
            Files.deleteIfExists(combinedFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String processForVisitorPattern(List<CompilationUnit> compilationUnits, CombinedTypeSolver combinedSolver) {
        MethodCallCollector methodCallCollector = new MethodCallCollector();
        ClassHierarchyExtractor classHierarchyExtractor = new ClassHierarchyExtractor();
        MethodInformationExtractor methodInformationExtractor = new MethodInformationExtractor();

        compilationUnits.forEach(cu -> {
            cu.accept(classHierarchyExtractor, null);
            cu.accept(methodCallCollector, null);
            cu.accept(methodInformationExtractor, null);
        });
        methodCallCollector.finalizeMaps();
        Map<String, Set<String>> methodInfo = methodInformationExtractor.getMethodInformation();
        Map<String, Set<String>> interactions = methodInformationExtractor.getInteractions();
        Map<String, Set<String>> candidates = methodCallCollector.getCandidates();
        Map<String, String> subclassToSuperclassMap = classHierarchyExtractor.getSubclassToSuperclassMap();

        DoubleDispatchDetector Ddd = new DoubleDispatchDetector(candidates, subclassToSuperclassMap);
        compilationUnits.forEach(cu -> cu.accept(Ddd, null));

        VisitorAnalyzer Vanalyzer = new VisitorAnalyzer(candidates, Ddd.getElementToVisitorMappings(), subclassToSuperclassMap, methodInfo, interactions);
        Vanalyzer.analyze();
        return Vanalyzer.getFormattedAnalysisResults();
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

    private String processForChainOfResponsibility(List<CompilationUnit> compilationUnits) {
        ChainExtractor chainExtractor = new ChainExtractor();

        compilationUnits.forEach(cu -> {
            cu.accept(chainExtractor, null);
        });

        StringBuilder chainResults = new StringBuilder();

        HandlerChainAnalyzer handlerChainAnalyzer = new HandlerChainAnalyzer(
                chainExtractor.getHandlerHierarchy(),
                chainExtractor.getBaseHandlers(),
                chainExtractor.getChain(),
                chainExtractor.getClients(),
                chainExtractor.getBaseHandlerResponsibilities(),
                chainExtractor.getChain().getConcreteHandlerResponsibilityMap());

        handlerChainAnalyzer.analyze();

        //chainResults.append(handlerChainAnalyzer)

        RequestPropagationAnalyzer requestPropagationAnalyzer = new RequestPropagationAnalyzer(
                chainExtractor.getHandlerHierarchy(),
                chainExtractor.getBaseHandlers(),
                chainExtractor.getChain(),
                chainExtractor.getClients(),
                chainExtractor.getRequestMethods(),
                chainExtractor.isCircularChain());

        requestPropagationAnalyzer.analyze();

        chainResults.append(requestPropagationAnalyzer.getFormattedAnalysisResults());

        return chainResults.toString();
    }
}
