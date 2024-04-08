package com.example.design_pattern_verifier.ChainOfResponsibility;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Chain;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.ChainExtractor;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.HandlerChainAnalyzer;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Responsibility;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HandlerChainAnalyzerTest {

    private JavaParser javaParser;

    private HandlerChainAnalyzer analyzer;
    private Map<String, String> handlerHierarchy;
    private List<String> baseHandlers;
    private Chain chain;
    private List<String> clients;
    private Map<String, List<Responsibility>> responsibilitiesMap;
    private Map<String, List<Responsibility>> concreteResponsibilitiesMap;
    private Map<String, String> chainObjects;

    @BeforeEach
    void setUp() {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedSolver));
        this.javaParser = new JavaParser(parserConfiguration);

        handlerHierarchy = new HashMap<>();
        baseHandlers = new ArrayList<>();
        chain = new Chain();
        clients = new ArrayList<>();
        responsibilitiesMap = new HashMap<>();
        concreteResponsibilitiesMap = new HashMap<>();
        chainObjects = new HashMap<>();

        baseHandlers.add("BaseHandler");
        handlerHierarchy.put("ConcreteHandler1", "BaseHandler");
        handlerHierarchy.put("ConcreteHandler2", "BaseHandler");

        Responsibility r1 = new Responsibility("handle", null, new BlockStmt());
        Responsibility r2 = new Responsibility("handle", null, new BlockStmt());
        List<Responsibility> responsibilities1 = new ArrayList<>();
        responsibilities1.add(r1);

        List<Responsibility> responsibilities2 = new ArrayList<>();
        responsibilities2.add(r2);

        concreteResponsibilitiesMap.put("ConcreteHandler1", responsibilities1);
        concreteResponsibilitiesMap.put("ConcreteHandler2", responsibilities2);

        chain.addHandler("ConcreteHandler1", responsibilities1);
        chain.addHandler("ConcreteHandler2", responsibilities2);

        chainObjects.put("h1", "ConcreteHandler1");
        chainObjects.put("h2", "ConcreteHandler2");

        clients.add("Client");

        responsibilitiesMap.put("BaseHandler", responsibilities1);

        analyzer = new HandlerChainAnalyzer(handlerHierarchy, baseHandlers, chain, clients, responsibilitiesMap, concreteResponsibilitiesMap, chainObjects);
    }

    private String analyzeChainOfResponsibility(String resourcePath) throws Exception {
        Resource resource = new ClassPathResource(resourcePath);
        Path directoryPath = Paths.get(resource.getURI());
        Path combinedFilePath = this.combineJavaFiles(directoryPath);

        ChainExtractor chainExtractor = new ChainExtractor();

        ParseResult<CompilationUnit> parseResult;
        try (InputStream fileInputStream = Files.newInputStream(combinedFilePath)) {
            parseResult = this.javaParser.parse(fileInputStream);
        } finally {
            Files.deleteIfExists(combinedFilePath);
        }

        Set<CompilationUnit> compilationUnits = new HashSet<>();
        parseResult.getResult().ifPresent(compilationUnits::add);

        compilationUnits.forEach(cu -> {
            cu.accept(chainExtractor, null);
        });

        HandlerChainAnalyzer handlerChainAnalyzer = new HandlerChainAnalyzer(
                chainExtractor.getHandlerHierarchy(),
                chainExtractor.getBaseHandlers(),
                chainExtractor.getChain(),
                chainExtractor.getClients(),
                chainExtractor.getBaseHandlerResponsibilities(),
                chainExtractor.getChain().getConcreteHandlerResponsibilityMap(),
                chainExtractor.getChainObjects()
        );

        handlerChainAnalyzer.analyze();

        return handlerChainAnalyzer.getFormattedAnalysisResults();
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
    void testAnalyzeRedundantResponsibilities() {
        assertDoesNotThrow(() -> analyzer.analyze());
        String analysisResults = analyzer.getFormattedAnalysisResults();
        assertTrue(analysisResults.contains("Redundancy Detected"));
    }


    @Test
    void testIsHandler() {
        assertTrue(analyzer.isHandler("BaseHandler"));
        assertTrue(analyzer.isHandler("ConcreteHandler1"));
        assertFalse(analyzer.isHandler("NonExistentHandler"));
    }

    @Test
    void testChainOfResponsibilityAnalysis_Correct() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/CorrectCor/ChainOfResponsibilityExample.java";
        String expected = this.normalizeString("Chain of Responsibility Analysis Results for Chain Structure: \n" +
                "Please evaluate if this is your intended structure since our tool can't tell if this was precisely your intended design. We can only approximate and detect certain redundancies in your handler structure.\n" +
                "Chain: handler to handler:\n" +
                "->errorLogger->fileLogger->consoleLogger\n" +
                " Handler hierarchy (ConcreteHandlerClass=BaseHandlerClass): \n" +
                "{ConsoleLogger=Logger, FileLogger=Logger, ErrorLogger=Logger}\n" +
                "Base Handler(s): [Logger]\n" +
                "Concrete Handlers: [consoleLogger, errorLogger, fileLogger]\n" +
                "Responsibilities extracted from base handler: [[setNextLogger, logMessage, getLevel, write]]\n" +
                "Client(s): [ChainOfResponsibilityExample]");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertEquals(expected, actual);
    }

    @Test
    void testChainOfResponsibilityAnalysis_Correct2() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/CorrectCor2/ChainOfResponsibilityExample2.java";
        String expected = this.normalizeString("Chain of Responsibility Analysis Results for Chain Structure: \n" +
                "Please evaluate if this is your intended structure since our tool can't tell if this was precisely your intended design. We can only approximate and detect certain redundancies in your handler structure.\n" +
                "Chain: handler to handler:\n" +
                "->chain50->chain20->chain10->chain5->chain1\n" +
                " Handler hierarchy (ConcreteHandlerClass=BaseHandlerClass): \n" +
                "{Dispense1=DispenseChain, Dispense20=DispenseChain, Dispense50=DispenseChain, Dispense10=DispenseChain, Dispense5=DispenseChain}\n" +
                "Base Handler(s): [DispenseChain]\n" +
                "Concrete Handlers: [chain5, chain50, chain20, chain1, chain10]\n" +
                "Responsibilities extracted from base handler: [[setNextChain, dispense]]\n" +
                "Client(s): [ChainOfResponsibilityExample2]");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertEquals(expected, actual);
    }

    @Test
    void testChainOfResponsibilityAnalysis_RedundantHandler() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/IncorrectCor_RedundantHandler/Currency.java";
        String expected = this.normalizeString("have a redundancy in their responsibility dispense");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertTrue(actual.contains(expected));
    }

    @Test
    void testChainOfResponsibilityAnalysis_RedundantHandler2() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/IncorrectCor_RedundantHandler2/ChainOfResponsibilityExample.java";
        String expected = this.normalizeString("have a redundancy in their responsibility getLevel");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertTrue(actual.contains(expected));
    }

    @Test
    void testChainOfResponsibilityAnalysis_RedundantHandler3() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/IncorrectCor_RedundantHandler3/ChainOfResponsibilityExample.java";
        String expected = this.normalizeString("Chain of Responsibility Analysis Results for Chain Structure: \n" +
                "Please evaluate if this is your intended structure since our tool can't tell if this was precisely your intended design. We can only approximate and detect certain redundancies in your handler structure.\n" +
                "Chain: handler to handler:\n" +
                "->errorLogger->fileLogger->consoleLogger\n" +
                " Handler hierarchy (ConcreteHandlerClass=BaseHandlerClass): \n" +
                "{ConsoleLogger=Logger, FileLogger=Logger, WhateverLogger=Logger, ErrorLogger=Logger}\n" +
                " ===> Redundancy Detected:Handler WhateverLogger is redundant since it is implemented but not used in the chain\n" +
                "Base Handler(s): [Logger]\n" +
                "Concrete Handlers: [consoleLogger, errorLogger, fileLogger]\n" +
                "Responsibilities extracted from base handler: [[setNextLogger, logMessage, getLevel, write]]\n" +
                "Client(s): [ChainOfResponsibilityExample]");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertEquals(expected, actual);
    }

    @Test
    void testChainOfResponsibilityAnalysis_RedundantHandler4() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/IncorrectCor_RedundantHandler4/ChainOfResponsibilityExample.java";
        String expected = this.normalizeString("have a redundancy in their responsibility write");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertTrue(actual.contains(expected));
    }

    @Test
    void testChainOfResponsibilityAnalysis_RedundantHandler5() throws Exception {
        String path = "static/ChainOfResponsibilityTestDirs/IncorrectCor_RedundantHandler5/ChainOfResponsibilityExample.java";
        String expected = this.normalizeString("have a redundancy in their responsibility write");
        String actual = this.normalizeString(this.analyzeChainOfResponsibility(path));
        assertTrue(actual.contains(expected));
    }



}
