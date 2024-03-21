package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

// TBD: collet types and fallback to it when method resolution fails
// 1. for (IElement element : elements) => element is type IElement when element.accept(visitor) is invoked
// 2. ((ElementA) element) => element is type ElementA

public class MethodCallCollector extends VoidVisitorAdapter<Void> {
    private Map<String, Set<String>> potentialVisitors = new HashMap<>();
    private Map<String, Set<String>> potentialElements = new HashMap<>();
    private Map<String, Map<String, Set<String>>> methodCalls = new HashMap<>();

    /**
     *  Iterates through all arguments of each method call expression, resolving the types of the arguments and the scope.
     *  If first parameter's type of the resolved method matches the argument type, it records the scope and argument types
     * as potential visitors and elements. 
     * @param n   The current method call expression node being visited. 
     * @param arg An optional argument that's part of the visitor pattern structure.
     */
    @Override
    public void visit(MethodCallExpr n, Void arg) {
        super.visit(n, arg);

        n.getArguments().forEach(argument -> {
            try {
                String argumentType = argument.calculateResolvedType().describe();
                n.getScope().ifPresent(scope -> {
                    try {
                        String scopeType = scope.calculateResolvedType().describe();
                        ResolvedMethodDeclaration resolvedMethod = n.resolve();
                        String methodName = resolvedMethod.getName();

                        String scopeTypeNormalized = this.normalizeTypeName(scopeType);
                        String argumentTypeNormalized = this.normalizeTypeName(argumentType);

                        if (resolvedMethod.getParam(0).getType().describe().equals(argumentType)) {
                            this.potentialVisitors.computeIfAbsent(scopeTypeNormalized, k -> new HashSet<>()).add(argumentTypeNormalized);
                            this.potentialElements.computeIfAbsent(argumentTypeNormalized, k -> new HashSet<>()).add(scopeTypeNormalized);
                        }

                        this.methodCalls.computeIfAbsent(scopeTypeNormalized, k -> new HashMap<>())
                                .computeIfAbsent(methodName, k -> new HashSet<>())
                                .add(argumentTypeNormalized);
                    } catch (Exception ex) {
                        System.err.println("Failed to resolve scope type in method call: " + n);
                    }
                });
            } catch (Exception ex) {
                System.err.println("Failed to resolve argument type in method call: " + n);
            }
        });
    }

    public void finalizeMaps() {
        Map<String, Set<String>> filteredVisitors = this.potentialVisitors.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("java."))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Set<String>> filteredElements = this.potentialElements.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("java."))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.potentialElements = filteredElements;
        this.potentialVisitors = filteredVisitors;
    }

    public Map<String, Set<String>> getPotentialVisitors() {
        return this.potentialVisitors;
    }

    public Map<String, Set<String>> getPotentialElements() {
        return this.potentialElements;
    }

    public Map<String, Map<String, Set<String>>> getMethodCalls() {
        return this.methodCalls;
    }

    private String normalizeTypeName(String typeName) {
        int lastDotIndex = typeName.lastIndexOf('.');
        return (lastDotIndex != -1) ? typeName.substring(lastDotIndex + 1) : typeName;
    }
}