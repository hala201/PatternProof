package com.example.design_pattern_verifier.VisitorPattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.example.design_pattern_verifier.service.VisitorPattern.ClassHierarchyExtractor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;

@SpringBootTest
public class ClassHierarchyExtractorTest {

    private JavaParser javaParser;

    @BeforeEach
    public void setup() {
        this.javaParser = new JavaParser();
    }

    private Map<String, String> extractClassHierarchy(String resourcePath) throws Exception {
            Resource resource = new ClassPathResource(resourcePath);
            Path startPath = Paths.get(resource.getURI());
            Map<String, String> subclassToSuperclassMap = new HashMap<>();

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream.filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try (InputStream fileInputStream = Files.newInputStream(path)) {
                        CompilationUnit cu = this.javaParser.parse(fileInputStream).getResult().get();

                        ClassHierarchyExtractor extractor = new ClassHierarchyExtractor();
                        VoidVisitor<Void> classNameCollector = extractor;
                        classNameCollector.visit(cu, null);

                        subclassToSuperclassMap.putAll(extractor.getSubclassToSuperclassMap());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        }
        return subclassToSuperclassMap;
    }

    @Test
    public void classHierarchyForInheritance_CorrectlyMapped() throws Exception {
        Map<String, String> subclassToSuperclassMap = this.extractClassHierarchy("static/VisitorTestDirs/vsrc1");

        assertTrue(subclassToSuperclassMap.containsKey("Clothing"));
        assertTrue(subclassToSuperclassMap.containsKey("Book"));
        assertTrue(subclassToSuperclassMap.containsKey("PriceVisitor"));

        assertEquals("Element", subclassToSuperclassMap.get("Clothing"));
        assertEquals("Element", subclassToSuperclassMap.get("Book"));
        assertEquals("Visitor", subclassToSuperclassMap.get("PriceVisitor"));
    }

    @Test
    public void classHierarchyForInheritance_CorrectlyMapped2() throws Exception {
        Map<String, String> subclassToSuperclassMap = this.extractClassHierarchy("static/VisitorTestDirs/vsrc3");

        assertTrue(subclassToSuperclassMap.containsKey("ElementB"));
        assertTrue(subclassToSuperclassMap.containsKey("ElementA"));
        assertTrue(subclassToSuperclassMap.containsKey("ConcreteVisitor"));

        assertEquals("IElement", subclassToSuperclassMap.get("ElementB"));
        assertEquals("IElement", subclassToSuperclassMap.get("ElementA"));
        assertEquals("IVisitor", subclassToSuperclassMap.get("ConcreteVisitor"));
    }

}
