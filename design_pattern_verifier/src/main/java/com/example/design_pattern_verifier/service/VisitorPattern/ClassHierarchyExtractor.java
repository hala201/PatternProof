package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassHierarchyExtractor extends VoidVisitorAdapter<Void> {
    private Map<String, String> subclassToSuperclassMap = new HashMap<>();

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);

        n.getExtendedTypes().forEach(ext -> {
            String subclass = n.getNameAsString();
            String superclass = ext.getNameAsString();
            this.subclassToSuperclassMap.put(subclass, superclass);
        });

        n.getImplementedTypes().forEach(impl -> {
            String subclass = n.getNameAsString();
            String iface = impl.getNameAsString();
            this.subclassToSuperclassMap.put(subclass, iface);
        });
    }

    public Map<String, String> getSubclassToSuperclassMap() {
        return this.subclassToSuperclassMap;
    }
}
