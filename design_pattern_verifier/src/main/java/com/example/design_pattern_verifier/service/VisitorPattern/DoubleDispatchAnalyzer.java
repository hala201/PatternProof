package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoubleDispatchAnalyzer {
    // methods per class/type for potential visitor elements
    private final Map<String, Set<String>> methodInformation;
    // Potential visitor types with their associated element types
    private final Map<String, Set<String>> potentialVisitors;
    // Potential element types with their associated visitor types
    private final Map<String, Set<String>> potentialElements;
    // Method calls within the analyzed code, mapping callers to methods and their argument types
    private final Map<String, Map<String, Set<String>>> methodCalls;
    // maps subclasses to their superclass to handle inheritance in pattern verification
    private final Map<String, String> subclassToSuperclassMap;
    // map of identified double dispatches to avoid duplicate output and later uses
    private Set<String> identifiedDoubleDispatches = new HashSet<>();

    public DoubleDispatchAnalyzer(Map<String, Set<String>> methodInformation,
                                  Map<String, Set<String>> potentialVisitors,
                                  Map<String, Set<String>> potentialElements,
                                  Map<String, Map<String, Set<String>>> methodCalls,
                                  Map<String, String> subclassToSuperclassMap) {
        this.methodInformation = methodInformation;
        this.potentialVisitors = potentialVisitors;
        this.potentialElements = potentialElements;
        this.methodCalls = methodCalls;
        this.subclassToSuperclassMap = subclassToSuperclassMap;
    }

    /**
     * Analyzes the code for the double dispatch:
     * 1. Verifies each potential visitor has the necessary visit methods for associated elements.
     * 2. Verifies each potential element has the necessary accept methods for associated visitors.
     * 3. Checks method calls to verify the double dispatch between elements and visitors:
     *    - Processes methods associated with a specific caller to verify double dispatch. This includes
     *      analyzing the methods that the caller invokes to ensure they participate in a double dispatch 
     *      relationship by calling the appropriate visit or accept methods on potential visitor or element types.
     *    - Processes all argument types for each method associated with the caller. Considers every parameter in the
     *      method's signature.
     *    - Processes visitor methods to verify the correct implementation of double dispatch. This step
     *      involves checking if visitor methods are properly calling the accept method on elements, and
     *      completing the double dispatch cycle.
     */
    public void analyze() {
        this.adjustForInheritance();

        this.processVisitorMethods();

        this.processAcceptMethods();

        this.processMethodCalls();
    }

    private void processVisitorMethods() {
        this.potentialVisitors.forEach((visitor, elements) -> {
            Set<String> methods = this.methodInformation.getOrDefault(visitor, Set.of());
            elements.forEach(element -> {
                boolean hasVisitMethod = methods.stream().anyMatch(method -> method.contains(element));
                if (!hasVisitMethod) {
                    System.out.println("Missing visit method in " + visitor + " for " + element);
                }
            });
        });
    }

    private void processAcceptMethods() { 
        this.potentialElements.forEach((element, visitors) -> {
            Set<String> methods = this.methodInformation.getOrDefault(element, Set.of());
            boolean hasAcceptMethod = methods.stream().anyMatch(method -> visitors.stream().anyMatch(method::contains));
            if (!hasAcceptMethod) {
                System.out.println("Missing accept method in " + element);
            }
        });
    }

    private void processMethodCalls() {
        this.methodCalls.forEach((caller, methods) -> {
            if (this.potentialElements.containsKey(caller)) {
                this.processMethodsForCaller(caller, methods);
            }
        });
    }

    private void processMethodsForCaller(String caller, Map<String, Set<String>> methods) {
        methods.forEach((methodName, argumentTypes) -> this.processArgumentTypesForMethod(caller, methodName, argumentTypes));
    }

    private void processArgumentTypesForMethod(String caller, String methodName, Set<String> argumentTypes) {
        argumentTypes.forEach(argumentType -> {
            if (this.potentialVisitors.containsKey(argumentType)) {
                this.processVisitorMethods(caller, argumentType);
            }
        });
    }

    private void processVisitorMethods(String caller, String argumentType) {
        Set<String> visitorMethods = this.methodInformation.get(argumentType);
        if (visitorMethods != null) {
            visitorMethods.forEach(visitorMethod -> this.analyzeDoubleDispatch(caller, argumentType, visitorMethod));
        }
    }

    private void analyzeDoubleDispatch(String caller, String argumentType, String visitorMethod) {
        String parametersString = visitorMethod.substring(visitorMethod.indexOf('(') + 1, visitorMethod.indexOf(')'));
        String[] params = parametersString.split(",");
        for (String param : params) {
            if (!param.isEmpty()) {
                String paramType = param.trim().split(" ")[0];
                if (this.potentialElements.containsKey(paramType) && !caller.equals(argumentType)) {
                    String relationshipIdentifier = caller + "->" + argumentType;
                    if (!this.identifiedDoubleDispatches.contains(relationshipIdentifier)) {
                        System.out.println("Double dispatch confirmed between " + caller + " and " + argumentType + " with parameter type " + paramType);
                        this.identifiedDoubleDispatches.add(relationshipIdentifier);
                    }
                }
            }
        }
    }
    
    private void adjustForInheritance() {
        Map<String, Set<String>> tempElements = new HashMap<>();
        Map<String, Set<String>> tempVisitors = new HashMap<>();

        System.out.println("Subclass to Superclass Map:");
        this.subclassToSuperclassMap.forEach((k,v) -> { System.out.println(k + " -> " + v); });

        for (Map.Entry<String, Set<String>> entry : this.potentialElements.entrySet()) {
            String element = entry.getKey();
            Set<String> visitors = entry.getValue();
            String superclass = this.subclassToSuperclassMap.get(element);
            while (superclass != null) {
                tempElements.computeIfAbsent(superclass, k -> new HashSet<>()).addAll(visitors);
                superclass = this.subclassToSuperclassMap.get(superclass);
            }
        }

        tempElements.forEach((key, value) -> this.potentialElements.computeIfAbsent(key, k -> new HashSet<>()).addAll(value));

        for (Map.Entry<String, String> entry : this.subclassToSuperclassMap.entrySet()) {
            String subclass = entry.getKey();
            String superclass = entry.getValue();
            if (this.potentialVisitors.containsKey(superclass)) {
                tempVisitors.computeIfAbsent(subclass, k -> new HashSet<>()).addAll(this.potentialVisitors.get(superclass));
            }
        }

        tempVisitors.forEach((key, value) -> this.potentialVisitors.computeIfAbsent(key, k -> new HashSet<>()).addAll(value));
    }

    public Set<String> getIdentifiedDoubleDispatches() {
        return this.identifiedDoubleDispatches;
    }
}
