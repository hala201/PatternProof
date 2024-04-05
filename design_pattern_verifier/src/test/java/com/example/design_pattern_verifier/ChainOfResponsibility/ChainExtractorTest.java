package com.example.design_pattern_verifier.ChainOfResponsibility;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.ChainExtractor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
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

public class ChainExtractorTest {

    private JavaParser javaParser;

    @BeforeEach
    public void setup() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
        this.javaParser = new JavaParser(parserConfiguration);
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

    public ChainExtractor getChainExtractor(String resourcePath) throws Exception {
        Resource resource = new ClassPathResource(resourcePath);
        Path directoryPath = Paths.get(resource.getURI());
        Path combinedFilePath = this.combineJavaFiles(directoryPath);
        ChainExtractor chainExtractor = new ChainExtractor();

        try (InputStream fileInputStream = Files.newInputStream(combinedFilePath)){
            CompilationUnit cu = this.javaParser.parse(fileInputStream).getResult().get();
            cu.accept(chainExtractor, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Files.deleteIfExists(combinedFilePath);
        }

        return chainExtractor;
    }

    @Test
    public void ChainExtractorTest_CorrectCor() throws Exception {
        ChainExtractor chainExtractor = getChainExtractor("static/ChainOfResponsibilityTestDirs/CorrectCor");

        assertTrue(chainExtractor.getClients().contains("ChainOfResponsibilityExample"));
        assertTrue(chainExtractor.getBaseHandlers().contains("Logger"));
        assertTrue(chainExtractor.getConfirmedBaseHandlers().contains("Logger"));
        assertTrue(chainExtractor.getBaseHandlerResponsibilities().containsKey("Logger"));
        System.out.println(chainExtractor.getBaseHandlerResponsibilities().values());
        String[] variables1 = {"loggerChain", "consoleLogger", "errorLogger", "fileLogger"};
        for (String var: variables1) {
            assertTrue(chainExtractor.getChainVariables().containsKey(var));
        }
        String[] handlers1 = {"Logger", "Logger", "Logger", "Logger", "Logger"};
        for (String han: handlers1) {
            assertTrue(chainExtractor.getChainVariables().containsValue(han));
        }
        String[] variables2 = {"consoleLogger", "errorLogger", "fileLogger"};
        for (String var: variables2) {
            assertTrue(chainExtractor.getChainObjects().containsKey(var));
        }
        String[] handlers2 = {"ConsoleLogger", "ErrorLogger", "FileLogger"};
        for (String han: handlers2) {
            assertTrue(chainExtractor.getChainObjects().containsValue(han));
        }
        String[] chain = {"errorLogger", "fileLogger", "consoleLogger"};
        assertArrayEquals(chainExtractor.getChain().getHandlerNames().toArray(), chain);

        assertTrue(chainExtractor.getRequestMethods().contains("logMessage"));
    }

    @Test
    public void ChainExtractorTest_CorrectCor2() throws Exception {
        ChainExtractor chainExtractor = getChainExtractor("static/ChainOfResponsibilityTestDirs/CorrectCor2");

        assertTrue(chainExtractor.getClients().contains("ChainOfResponsibilityExample2"));
        assertTrue(chainExtractor.getBaseHandlers().contains("DispenseChain"));
        assertTrue(chainExtractor.getConfirmedBaseHandlers().contains("DispenseChain"));
        assertTrue(chainExtractor.getBaseHandlerResponsibilities().containsKey("DispenseChain"));
        System.out.println(chainExtractor.getBaseHandlerResponsibilities().values());
        String[] variables = {"chain5", "chain50", "chain1", "chain20", "chain10"};
        for (String var: variables) {
            assertTrue(chainExtractor.getChainVariables().containsKey(var));
            assertTrue(chainExtractor.getChainObjects().containsKey(var));
        }
        String[] handlers1 = {"Dispense5", "DispenseChain", "Dispense20", "Dispense1", "DispenseChain"};
        for (String han: handlers1) {
            assertTrue(chainExtractor.getChainVariables().containsValue(han));
        }
        String[] handlers2 = {"Dispense5", "Dispense20", "Dispense20", "Dispense1", "Dispense10"};
        for (String han: handlers2) {
            assertTrue(chainExtractor.getChainObjects().containsValue(han));
        }
        String[] chain = {"chain50", "chain20", "chain10", "chain1", "chain5"};
        assertArrayEquals(chainExtractor.getChain().getHandlerNames().toArray(), chain);

        assertTrue(chainExtractor.getRequestMethods().contains("dispense"));
    }


    @Test
    public void ChainExtractorTest_IncorrectCor_RedundantHandler() throws Exception {
        ChainExtractor chainExtractor = getChainExtractor("static/ChainOfResponsibilityTestDirs/IncorrectCor_RedundantHandler");

        assertTrue(chainExtractor.getClients().contains("ATMDispenseChain"));
        assertTrue(chainExtractor.getBaseHandlers().contains("DispenseChain"));
        assertTrue(chainExtractor.getConfirmedBaseHandlers().contains("DispenseChain"));
        assertTrue(chainExtractor.getBaseHandlerResponsibilities().containsKey("DispenseChain"));
        System.out.println(chainExtractor.getBaseHandlerResponsibilities().values());
        String[] variables = {"c3", "c1", "c2"};
        for (String var: variables) {
            assertTrue(chainExtractor.getChainVariables().containsKey(var));
        }
        String[] handlers1 = {"DispenseChain", "DispenseChain", "DispenseChain"};
        for (String han: handlers1) {
            assertTrue(chainExtractor.getChainVariables().containsValue(han));
        }
        String[] variables2 = {"c1", "c2", "c3"};
        for (String var: variables2) {
            assertTrue(chainExtractor.getChainObjects().containsKey(var));
        }
        String[] handlers2 = {"Dollar10Dispenser", "Dollar50Dispenser", "Dollar20Dispenser"};
        for (String han: handlers2) {
            assertTrue(chainExtractor.getChainObjects().containsValue(han));
        }
        String[] chain = {"c1", "c2", "c3"};
        assertArrayEquals(chainExtractor.getChain().getHandlerNames().toArray(), chain);

        assertTrue(chainExtractor.getRequestMethods().contains("dispense"));
    }

    @Test
    public void ChainExtractorTest_IncorrectCor_UnhandledRequest() throws Exception {
        ChainExtractor chainExtractor = getChainExtractor("static/ChainOfResponsibilityTestDirs/IncorrectCor_UnhandledRequest");

        assertTrue(chainExtractor.getClients().contains("ChainOfResponsibilityExample"));
        assertTrue(chainExtractor.getBaseHandlers().contains("Logger"));
        assertTrue(chainExtractor.getConfirmedBaseHandlers().contains("Logger"));
        assertTrue(chainExtractor.getBaseHandlerResponsibilities().containsKey("Logger"));
        System.out.println(chainExtractor.getBaseHandlerResponsibilities().values());
        String[] variables1 = {"loggerChain", "consoleLogger", "errorLogger", "fileLogger"};
        for (String var: variables1) {
            assertTrue(chainExtractor.getChainVariables().containsKey(var));
        }
        String[] handlers1 = {"Logger", "Logger", "Logger", "Logger", "Logger"};
        for (String han: handlers1) {
            assertTrue(chainExtractor.getChainVariables().containsValue(han));
        }
        String[] variables2 = {"consoleLogger", "errorLogger", "fileLogger"};
        for (String var: variables2) {
            assertTrue(chainExtractor.getChainObjects().containsKey(var));
        }
        String[] handlers2 = {"ConsoleLogger", "ErrorLogger", "FileLogger"};
        for (String han: handlers2) {
            assertTrue(chainExtractor.getChainObjects().containsValue(han));
        }
        String[] chain = {"errorLogger", "fileLogger", "consoleLogger"};
        assertArrayEquals(chainExtractor.getChain().getHandlerNames().toArray(), chain);

        assertTrue(chainExtractor.getRequestMethods().contains("logMessage"));
    }

}
