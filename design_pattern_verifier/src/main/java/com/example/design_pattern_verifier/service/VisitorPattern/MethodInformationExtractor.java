package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodInformationExtractor extends VoidVisitorAdapter<Void> {
    private Map<String, Set<String>> methodInformation = new HashMap<>();
    private Map<String, Set<String>> interactions = new HashMap<>();

    /**
     * For each method, extracts the class name and method signature
     * @param n   The class or interface declaration node to visit.
     * @param arg The argument passed to the visit method.
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        super.visit(n, arg);

        n.getMethods().forEach(method -> {
            String className = n.getNameAsString();
            String methodSignature = this.extractMethodSignature(method);
            this.methodInformation.computeIfAbsent(className, k -> new HashSet<>()).add(methodSignature);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);
        n.findAncestor(MethodDeclaration.class).ifPresent(methodDeclaration -> {
            String methodName = methodDeclaration.getNameAsString();
            methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(classDeclaration -> {
                String className = classDeclaration.getNameAsString();
                String key = className + "." + methodName; 
                this.interactions.computeIfAbsent(key, k -> new HashSet<>()).add(n.toString());
            });
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(CastExpr n, Void arg) {
        super.visit(n, arg);
        n.findAncestor(MethodDeclaration.class).ifPresent(methodDeclaration -> {
            String methodName = methodDeclaration.getNameAsString();
            methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(classDeclaration -> {
                String className = classDeclaration.getNameAsString();
                String key = className + "." + methodName;
                String castDetail = "Cast to " + n.getType() + " in " + key;
                this.interactions.computeIfAbsent(key, k -> new HashSet<>()).add(castDetail);
            });
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(InstanceOfExpr n, Void arg) {
        super.visit(n, arg);
        n.findAncestor(MethodDeclaration.class).ifPresent(methodDeclaration -> {
            String methodName = methodDeclaration.getNameAsString();
            methodDeclaration.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(classDeclaration -> {
                String className = classDeclaration.getNameAsString();
                String key = className + "." + methodName;
                this.interactions.computeIfAbsent(key, k -> new HashSet<>()).add(n.toString());
            });
        });
    }

    /**
     * Extracts the method signature from a given MethodDeclaration object.
     * @param method The MethodDeclaration object to extract the signature from.
     * @return The method signature as a string.
     */
    public String extractMethodSignature(MethodDeclaration method) {
        StringBuilder signatureBuilder = new StringBuilder(method.getNameAsString());
        signatureBuilder.append("(");
        method.getParameters().forEach(param -> {
            String typeName = this.normalizeTypeName(param.getType().asString());
            signatureBuilder.append(typeName).append(" ").append(param.getName()).append(", ");
        });
        if (!method.getParameters().isEmpty()) {
            signatureBuilder.setLength(signatureBuilder.length() - 2); // Remove trailing comma and space
        }
        signatureBuilder.append(")");
        return signatureBuilder.toString();
    }
    
    private String normalizeTypeName(String typeName) {
        int genericStartIndex = typeName.indexOf('<');
        if (genericStartIndex != -1) {
            typeName = typeName.substring(0, genericStartIndex);
        }
        int lastDotIndex = typeName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            typeName = typeName.substring(lastDotIndex + 1);
        }
        return typeName;
    }
    
    public Map<String, Set<String>> getMethodInformation() {
        return this.methodInformation;
    }

    public Map<String, Set<String>> getInteractions() {
        return this.interactions;
    }
}
