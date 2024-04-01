package com.example.design_pattern_verifier.VisitorPattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.example.design_pattern_verifier.service.VisitorPattern.MethodInformationExtractor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import java.util.HashMap;
        import java.util.Map;

@SpringBootTest
public class MethodInformationExtractorTest {

    private JavaParser javaParser;

    @BeforeEach
    public void setup() {
        this.javaParser = new JavaParser();
    }

    private Map<String, Set<String>> extractMethodInfo(String resourcePath) throws Exception {
        Resource resource = new ClassPathResource(resourcePath);
        Path startPath = Paths.get(resource.getURI());
        Map<String, Set<String>> methodInfo = new HashMap<>();
        try (Stream<Path> stream = Files.walk(startPath)) {
            stream.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try (InputStream fileInputStream = Files.newInputStream(path)) {
                            CompilationUnit cu = this.javaParser.parse(fileInputStream).getResult().get();

                            MethodInformationExtractor extractor = new MethodInformationExtractor();
                            VoidVisitor<Void> classNameCollector = extractor;
                            classNameCollector.visit(cu, null);
                            methodInfo.putAll(extractor.getMethodInformation());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return methodInfo;
    }

    @Test
    public void MethodInformationExtractorTest_vsrc1() throws Exception {
        Map<String, Set<String>> methodInfo = this.extractMethodInfo("static/VisitorTestDirs/vsrc1");

        assertTrue(methodInfo.size() == 6);
        assertTrue(methodInfo.containsKey("Visitor"));
        assertTrue(methodInfo.get("Visitor").contains("visit(Book book)"));
        assertTrue(methodInfo.get("Visitor").contains("visit(Clothing clothing)"));

        assertTrue(methodInfo.containsKey("Clothing"));
        assertTrue(methodInfo.get("Clothing").contains("getPrice()"));
        assertTrue(methodInfo.get("Clothing").contains("getSize()"));
        assertTrue(methodInfo.get("Clothing").contains("accept(Visitor visitor)"));

        assertTrue(methodInfo.containsKey("Visitor1"));
        assertTrue(methodInfo.get("Visitor1").contains("main(String[] args)"));

        assertTrue(methodInfo.containsKey("PriceVisitor"));
        assertTrue(methodInfo.get("PriceVisitor").contains("visit(Book book)"));
        assertTrue(methodInfo.get("PriceVisitor").contains("visit(Clothing clothing)"));
        assertTrue(methodInfo.get("PriceVisitor").contains("getTotalPrice()"));

        assertTrue(methodInfo.containsKey("Book"));
        assertTrue(methodInfo.get("Book").contains("getPrice()"));
        assertTrue(methodInfo.get("Book").contains("getIsbnNumber()"));
        assertTrue(methodInfo.get("Book").contains("accept(Visitor visitor)"));

        assertTrue(methodInfo.containsKey("Element"));
        assertTrue(methodInfo.get("Element").contains("accept(Visitor visitor)"));

//        for (Map.Entry<String, Set<String>> entry : methodInfo.entrySet())
//        {
//            String key = entry.getKey();
//            Set<String> value = entry.getValue();
//            System.out.println("*****************************");
//            System.out.println("Class: " + key);
//            System.out.println("Method: ");
//            for (String v : value)
//            {
//                System.out.println(v);
//            }
//        }
    }

    @Test
    public void MethodInformationExtractorTest_vsrc2() throws Exception {
        Map<String, Set<String>> methodInfo = this.extractMethodInfo("static/VisitorTestDirs/vsrc2");

        assertTrue(methodInfo.size() == 6);
        assertTrue(methodInfo.containsKey("IElement"));
        assertTrue(methodInfo.get("IElement").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("ConcreteVisitor"));
        assertTrue(methodInfo.get("ConcreteVisitor").contains("visit(ElementA element)"));
        assertTrue(methodInfo.get("ConcreteVisitor").contains("visit(ElementB element)"));

        assertTrue(methodInfo.containsKey("ElementB"));
        assertTrue(methodInfo.get("ElementB").contains("getFromA()"));
        assertTrue(methodInfo.get("ElementB").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("IVisitor"));
        assertTrue(methodInfo.get("IVisitor").contains("visit(ElementA element)"));
        assertTrue(methodInfo.get("IVisitor").contains("visit(ElementB element)"));

        assertTrue(methodInfo.containsKey("Main"));
        assertTrue(methodInfo.get("Main").contains("main(String[] args)"));

        assertTrue(methodInfo.containsKey("ElementA"));
        assertTrue(methodInfo.get("ElementA").contains("getFromA()"));
        assertTrue(methodInfo.get("ElementA").contains("accept(IVisitor visitor)"));
    }

    @Test
    public void MethodInformationExtractorTest_vsrc3() throws Exception {
        Map<String, Set<String>> methodInfo = this.extractMethodInfo("static/VisitorTestDirs/vsrc3");

        assertTrue(methodInfo.size() == 6);
        assertTrue(methodInfo.containsKey("IElement"));
        assertTrue(methodInfo.get("IElement").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("ConcreteVisitor"));
        assertTrue(methodInfo.get("ConcreteVisitor").contains("visit(IElement element)"));

        assertTrue(methodInfo.containsKey("ElementB"));
        assertTrue(methodInfo.get("ElementB").contains("exclusiveMethodOfElementB()"));
        assertTrue(methodInfo.get("ElementB").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("IVisitor"));
        assertTrue(methodInfo.get("IVisitor").contains("visit(IElement element)"));

        assertTrue(methodInfo.containsKey("Main"));
        assertTrue(methodInfo.get("Main").contains("main(String[] args)"));

        assertTrue(methodInfo.containsKey("ElementA"));
        assertTrue(methodInfo.get("ElementA").contains("exclusiveMethodOfElementA()"));
        assertTrue(methodInfo.get("ElementA").contains("accept(IVisitor visitor)"));
    }

    @Test
    public void MethodInformationExtractorTest_vsrc4() throws Exception {
        Map<String, Set<String>> methodInfo = this.extractMethodInfo("static/VisitorTestDirs/vsrc4");

        assertTrue(methodInfo.size() == 5);
        assertTrue(methodInfo.containsKey("A1"));
        assertTrue(methodInfo.get("A1").contains("foo(B b)"));
        assertTrue(methodInfo.get("A1").contains("get()"));

        assertTrue(methodInfo.containsKey("A"));
        assertTrue(methodInfo.get("A").contains("foo(B b)"));

        assertTrue(methodInfo.containsKey("B"));
        assertTrue(methodInfo.get("B").contains("bee(A1 book)"));

        assertTrue(methodInfo.containsKey("Visitor1"));
        assertTrue(methodInfo.get("Visitor1").contains("main(String[] args)"));

        assertTrue(methodInfo.containsKey("B1"));
        assertTrue(methodInfo.get("B1").contains("bee(A1 a)"));
    }

    @Test
    public void MethodInformationExtractorTest_vsrc5() throws Exception {
        Map<String, Set<String>> methodInfo = this.extractMethodInfo("static/VisitorTestDirs/vsrc5");

        assertTrue(methodInfo.size() == 6);
        assertTrue(methodInfo.containsKey("IElement"));
        assertTrue(methodInfo.get("IElement").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("ConcreteVisitor"));
        assertTrue(methodInfo.get("ConcreteVisitor").contains("visit(IElement element)"));

        assertTrue(methodInfo.containsKey("ElementB"));
        assertTrue(methodInfo.get("ElementB").contains("exclusiveMethodOfElementB()"));
        assertTrue(methodInfo.get("ElementB").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("IVisitor"));
        assertTrue(methodInfo.get("IVisitor").contains("visit(IElement element)"));

        assertTrue(methodInfo.containsKey("ElementA"));
        assertTrue(methodInfo.get("ElementA").contains("exclusiveMethodOfElementA()"));
        assertTrue(methodInfo.get("ElementA").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("Main"));
        assertTrue(methodInfo.get("Main").contains("main(String[] args)"));
    }

    @Test
    public void MethodInformationExtractorTest_vsrc6() throws Exception {
        Map<String, Set<String>> methodInfo = this.extractMethodInfo("static/VisitorTestDirs/vsrc6");

        assertTrue(methodInfo.size() == 7);
        assertTrue(methodInfo.containsKey("IElement"));
        assertTrue(methodInfo.get("IElement").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("ConcreteVisitor"));
        assertTrue(methodInfo.get("ConcreteVisitor").contains("visit(ElementA element)"));
        assertTrue(methodInfo.get("ConcreteVisitor").contains("visit(ElementB element)"));

        assertTrue(methodInfo.containsKey("IVisitor"));
        assertTrue(methodInfo.get("IVisitor").contains("visit(ElementA element)"));
        assertTrue(methodInfo.get("IVisitor").contains("visit(ElementB element)"));

        assertTrue(methodInfo.containsKey("ElementC"));
        assertTrue(methodInfo.get("ElementC").contains("getFromA()"));
        assertTrue(methodInfo.get("ElementC").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("ElementB"));
        assertTrue(methodInfo.get("ElementB").contains("getFromA()"));
        assertTrue(methodInfo.get("ElementB").contains("accept(IVisitor visitor)"));

        assertTrue(methodInfo.containsKey("Main"));
        assertTrue(methodInfo.get("Main").contains("main(String[] args)"));

        assertTrue(methodInfo.containsKey("ElementA"));
        assertTrue(methodInfo.get("ElementA").contains("getFromA()"));
        assertTrue(methodInfo.get("ElementA").contains("accept(IVisitor visitor)"));
    }
}
