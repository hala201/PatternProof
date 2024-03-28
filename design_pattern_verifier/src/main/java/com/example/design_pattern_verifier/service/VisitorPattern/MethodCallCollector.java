package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;


public class MethodCallCollector extends VoidVisitorAdapter<Void> {
    private Map<String, Set<String>> candidates = new HashMap<>();
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
                            this.candidates.computeIfAbsent(scopeTypeNormalized, k -> new HashSet<>()).add(argumentTypeNormalized);
                            this.candidates.computeIfAbsent(argumentTypeNormalized, k -> new HashSet<>()).add(scopeTypeNormalized);
                        }

                        this.methodCalls.computeIfAbsent(scopeTypeNormalized, k -> new HashMap<>())
                                .computeIfAbsent(methodName, k -> new HashSet<>())
                                .add(argumentTypeNormalized);
                    } catch (Exception ex) {
                        // System.err.println("Failed to resolve scope type in method call: " + n);
                    }
                });
            } catch (Exception ex) {
                // System.err.println("Failed to resolve argument type in method call: " + n);
            }
        });
    }

    public void finalizeMaps() {
        Map<String, Set<String>> filteredCandidates = this.candidates.entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith("java."))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.candidates = filteredCandidates;
    }

    public Map<String, Set<String>> getCandidates() {
        return this.candidates;
    }

    public Map<String, Map<String, Set<String>>> getMethodCalls() {
        return this.methodCalls;
    }

    private String normalizeTypeName(String typeName) {
        int lastDotIndex = typeName.lastIndexOf('.');
        String simpleName = (lastDotIndex != -1) ? typeName.substring(lastDotIndex + 1) : typeName;
        int genericStartIndex = simpleName.indexOf('<');
        if (genericStartIndex != -1) {
            simpleName = simpleName.substring(0, genericStartIndex);
        }
        return simpleName;
    }
}