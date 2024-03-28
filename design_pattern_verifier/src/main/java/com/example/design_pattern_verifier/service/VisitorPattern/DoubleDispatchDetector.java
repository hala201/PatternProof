package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class DoubleDispatchDetector extends VoidVisitorAdapter<Void> {
    private Map<String, Set<String>> candidates;
    private Map<String, Set<String>> elementToVisitorMappings = new HashMap<>();
    private Map<String, String> subclassToSuperclassMap;

    public DoubleDispatchDetector(Map<String, Set<String>> candidates, Map<String, String> subclassToSuperclassMap) {
        this.candidates = candidates;
        this.subclassToSuperclassMap = subclassToSuperclassMap;
    }

    /**
     * Visits method declarations to identify and map double dispatch interactions.
     */
    @Override
    public void visit(MethodDeclaration n, Void arg) {
        super.visit(n, arg);

        n.getBody().ifPresent(body -> {
            try {
                body.getStatements().forEach(statement -> {
                    statement.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        String callerClassName = this.findClassName(n);
                        String calleeClassName = methodCall.resolve().declaringType().getQualifiedName();
                        
                        // System.out.println("Caller: " + callerClassName + " | Callee: " + calleeClassName + " | " + n.getParameters());
                        if (this.isCandidatePair(callerClassName, calleeClassName)) {
                            methodCall.getArguments().forEach(argument -> {
                                if (argument.toString().equals("this")) {
                                    if (this.calleeMatchesMethodParameter(n, calleeClassName)) {
                                        this.checkAndMap(callerClassName, calleeClassName);
                                    }
                                }
                            });
                        }
                    });
                });
            } catch (Exception ex) {
                // System.err.println("Failed to resolve method call in method: " + n);
            }
        });
    }

    /**
     * Determines if a caller-callee pair is considered a candidate for double dispatch.
     */
    private boolean isCandidatePair(String caller, String callee) {
        if (this.directOrInheritedCandidate(caller, callee)) {
            return true;
        }
        
        return this.checkCallerInheritanceForCandidates(caller, callee);
    }
    
    /**
     * Checks direct or inherited relationships between caller and callee as candidates.
     */
    private boolean directOrInheritedCandidate(String caller, String callee) {
        Set<String> callerCandidates = this.candidates.get(caller);
        if (callerCandidates != null && (callerCandidates.contains(callee) || callerCandidates.stream().anyMatch(c -> this.isRelated(c, callee) || this.isRelated(callee, c)))) {
            return true;
        }
    
        for (String superCallee : this.subclassToSuperclassMap.keySet()) {
            if (this.isRelated(superCallee, callee) || this.isRelated(callee, superCallee)) {
                if (callerCandidates != null && callerCandidates.contains(superCallee)) {
                    return true;
                }
            }
        }
    
        return false;
    }
    
    /**
     * Examines inheritance chains to identify potential candidate relationships.
     */
    private boolean checkCallerInheritanceForCandidates(String caller, String callee) {
        for (Map.Entry<String, Set<String>> entry : this.candidates.entrySet()) {
            if (this.isRelated(entry.getKey(), caller) || this.isRelated(caller, entry.getKey())) {
                if (entry.getValue().contains(callee) || entry.getValue().stream().anyMatch(c -> this.isRelated(c, callee) || this.isRelated(callee, c))) {
                    return true;
                }
            }
        }
    
        return false;
    }
    
    /**
     * Checks if a callee matches or is related to a method's parameter.
     */
    private boolean calleeMatchesMethodParameter(MethodDeclaration methodDeclaration, String calleeClassName) {
        return methodDeclaration.getParameters().stream().anyMatch(param -> {
            String paramTypeName = param.getType().resolve().describe();
            paramTypeName = this.normalizeTypeName(paramTypeName);
    
            if (paramTypeName.equals(calleeClassName)) {
                return true;
            }
    
            return this.isRelated(calleeClassName, paramTypeName) || this.isRelated(paramTypeName, calleeClassName);
        });
    }
    
    private boolean isRelated(String classA, String classB) {
        if (classA.equals(classB)) {
            return true;
        }
    
        String currentClass = classA;
        while (currentClass != null && this.subclassToSuperclassMap.containsKey(currentClass)) {
            if (this.subclassToSuperclassMap.get(currentClass).equals(classB)) {
                return true;
            }
            currentClass = this.subclassToSuperclassMap.get(currentClass);
        }
    
        currentClass = classB;
        while (currentClass != null && this.subclassToSuperclassMap.containsKey(currentClass)) {
            if (this.subclassToSuperclassMap.get(currentClass).equals(classA)) {
                return true;
            }
            currentClass = this.subclassToSuperclassMap.get(currentClass);
        }
    
        return false;
    }
    
    private void checkAndMap(String callerClassName, String calleeClassName) {
        for (String candidate : this.candidates.keySet()) {
            if (this.isRelated(candidate, callerClassName) || this.isRelated(candidate, calleeClassName)) {
                this.candidates.get(candidate).forEach(relatedCandidate -> {
                    if (this.isRelated(relatedCandidate, callerClassName) || this.isRelated(relatedCandidate, calleeClassName)) {
                        this.mapCallerToCalleeAndItsInheritance(callerClassName, calleeClassName);
                    }
                });
            }
        }
    }
    
    private void mapCallerToCalleeAndItsInheritance(String callerClassName, String calleeClassName) {
        this.addMapping(calleeClassName, callerClassName);
    
        String currentSuperclass = this.subclassToSuperclassMap.get(calleeClassName);
        while (currentSuperclass != null) {
            this.addMapping(currentSuperclass, callerClassName);
            currentSuperclass = this.subclassToSuperclassMap.get(currentSuperclass);
        }

        this.subclassToSuperclassMap.forEach((subclass, superclass) -> {
            if (this.isDescendantOf(calleeClassName, subclass)) {
                this.addMapping(subclass, callerClassName);
            }
        });
    }
    
    private void addMapping(String callee, String caller) {
        this.elementToVisitorMappings.computeIfAbsent(callee, k -> new HashSet<>()).add(caller);
    }
    
    private boolean isDescendantOf(String superclass, String subclass) {
        String currentClass = subclass;
        while (currentClass != null) {
            if (this.subclassToSuperclassMap.get(currentClass) == null) {
                return false;
            }
            if (this.subclassToSuperclassMap.get(currentClass).equals(superclass)) {
                return true;
            }
            currentClass = this.subclassToSuperclassMap.get(currentClass);
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private String findClassName(MethodDeclaration n) {
        return n.findAncestor(ClassOrInterfaceDeclaration.class)
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElse(null);
    }

    public Map<String, Set<String>> getElementToVisitorMappings() {
        return this.elementToVisitorMappings;
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
}