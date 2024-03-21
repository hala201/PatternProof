package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class MethodInformationExtractor extends VoidVisitorAdapter<Void> {
    private Map<String, Set<String>> methodInformation = new HashMap<>();

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

    /**
     * Extracts the method signature from a given MethodDeclaration object.
     * @param method The MethodDeclaration object to extract the signature from.
     * @return The method signature as a string.
     */
    private String extractMethodSignature(MethodDeclaration method) {
        StringBuilder signatureBuilder = new StringBuilder(method.getNameAsString());
        signatureBuilder.append("(");
        method.getParameters().forEach(param -> {
            signatureBuilder.append(param.getType()).append(" ").append(param.getName()).append(", ");
        });
        if (!method.getParameters().isEmpty()) {
            signatureBuilder.setLength(signatureBuilder.length() - 2);
        }
        signatureBuilder.append(")");
        return signatureBuilder.toString();
    }

    public Map<String, Set<String>> getMethodInformation() {
        return this.methodInformation;
    }
}
