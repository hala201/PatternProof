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

import com.example.design_pattern_verifier.service.VisitorPattern.MethodCallCollector;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

@SpringBootTest
public class MethodCallCollectorTest {

    private JavaParser javaParser;

    @BeforeEach
    public void setup() {
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        ParserConfiguration parserConfiguration = new ParserConfiguration().setSymbolResolver(new JavaSymbolSolver(combinedSolver));
        this.javaParser = new JavaParser(parserConfiguration);
    }

    public Map<String, Set<String>> extractMethodCallCollector(String resourcePath) throws Exception {
        Resource resource = new ClassPathResource(resourcePath);
        Path directoryPath = Paths.get(resource.getURI());
        Path combinedFilePath = this.combineJavaFiles(directoryPath);
        Map<String, Set<String>> candidates = new HashMap<>();

        try (InputStream fileInputStream = Files.newInputStream(combinedFilePath)) {
            CompilationUnit cu = this.javaParser.parse(fileInputStream).getResult().get();
            MethodCallCollector methodCallCollector = new MethodCallCollector();
            cu.accept(methodCallCollector, null);
            methodCallCollector.finalizeMaps();

            candidates.putAll(methodCallCollector.getCandidates());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Files.deleteIfExists(combinedFilePath);
        }

        return candidates;
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
    public void MethodCallCollectorTest_vsr1() throws Exception {
        Map<String, Set<String>> result = this.extractMethodCallCollector("static/VisitorTestDirs/vsrc1");
        assertTrue(result.size() == 5);
        assertTrue(result.containsKey("PrintStream"));
        assertTrue(result.get("PrintStream").contains("String"));

        assertTrue(result.containsKey("Visitor"));
        assertTrue(result.get("Visitor").contains("Clothing"));
        assertTrue(result.get("Visitor").contains("Clothing"));

        assertTrue(result.containsKey("Clothing"));
        assertTrue(result.get("Clothing").contains("Visitor"));

        assertTrue(result.containsKey("Book"));
        assertTrue(result.get("Book").contains("Visitor"));

        assertTrue(result.containsKey("String"));
        assertTrue(result.get("String").contains("PrintStream"));
    }

    @Test
    public void MethodCallCollectorTest_vsr2() throws Exception {
        Map<String, Set<String>> result = this.extractMethodCallCollector("static/VisitorTestDirs/vsrc2");
        assertTrue(result.size() == 4);
        assertTrue(result.containsKey("IElement"));
        assertTrue(result.get("IElement").contains("IVisitor"));

        assertTrue(result.containsKey("IVisitor"));
        assertTrue(result.get("IVisitor").contains("IElement"));
        assertTrue(result.get("IVisitor").contains("ElementB"));
        assertTrue(result.get("IVisitor").contains("ElementA"));

        assertTrue(result.containsKey("ElementB"));
        assertTrue(result.get("ElementB").contains("IVisitor"));

        assertTrue(result.containsKey("ElementA"));
        assertTrue(result.get("ElementA").contains("IVisitor"));
    }

    @Test
    public void MethodCallCollectorTest_vsr3() throws Exception {
        Map<String, Set<String>> result = this.extractMethodCallCollector("static/VisitorTestDirs/vsrc3");
        assertTrue(result.size() == 4);
        assertTrue(result.containsKey("PrintStream"));
        assertTrue(result.get("PrintStream").contains("String"));

        assertTrue(result.containsKey("IElement"));
        assertTrue(result.get("IElement").contains("IVisitor"));

        assertTrue(result.containsKey("IVisitor"));
        assertTrue(result.get("IVisitor").contains("IElement"));

        assertTrue(result.containsKey("String"));
        assertTrue(result.get("String").contains("PrintStream"));
    }

    @Test
    public void MethodCallCollectorTest_vsr4() throws Exception {
        Map<String, Set<String>> result = this.extractMethodCallCollector("static/VisitorTestDirs/vsrc4");
        assertTrue(result.size() == 2);
        assertTrue(result.containsKey("A1"));
        assertTrue(result.get("A1").contains("B"));

        assertTrue(result.containsKey("B"));
        assertTrue(result.get("B").contains("A1"));
    }

    @Test
    public void MethodCallCollectorTest_vsr5() throws Exception {
        Map<String, Set<String>> result = this.extractMethodCallCollector("static/VisitorTestDirs/vsrc5");
        assertTrue(result.size() == 4);
        assertTrue(result.containsKey("PrintStream"));
        assertTrue(result.get("PrintStream").contains("String"));

        assertTrue(result.containsKey("IElement"));
        assertTrue(result.get("IElement").contains("IVisitor"));

        assertTrue(result.containsKey("IVisitor"));
        assertTrue(result.get("IVisitor").contains("IElement"));

        assertTrue(result.containsKey("String"));
        assertTrue(result.get("String").contains("PrintStream"));
    }

    @Test
    public void MethodCallCollectorTest_vsr6() throws Exception {
        Map<String, Set<String>> result = this.extractMethodCallCollector("static/VisitorTestDirs/vsrc6");
        assertTrue(result.size() == 4);
        assertTrue(result.containsKey("IElement"));
        assertTrue(result.get("IElement").contains("IVisitor"));

        assertTrue(result.containsKey("IVisitor"));
        assertTrue(result.get("IVisitor").contains("IElement"));
        assertTrue(result.get("IVisitor").contains("ElementB"));
        assertTrue(result.get("IVisitor").contains("ElementA"));

        assertTrue(result.containsKey("ElementB"));
        assertTrue(result.get("ElementB").contains("IVisitor"));

        assertTrue(result.containsKey("ElementA"));
        assertTrue(result.get("ElementA").contains("IVisitor"));
    }
}
