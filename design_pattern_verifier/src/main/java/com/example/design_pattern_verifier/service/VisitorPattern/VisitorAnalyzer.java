package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VisitorAnalyzer {
    private Map<String, Set<String>> candidates = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> elementToVisitorMappings = new HashMap<String, Set<String>>();
    private Map<String, String> subclassToSuperclassMap = new HashMap<String, String>();
    private Map<String, Set<String>> methodInfo = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> interactions = new HashMap<String, Set<String>>();
    private Map<String, Set<String>> logsMap = new HashMap<String, Set<String>>();
    
    /**
     * Initializes the VisitorAnalyzer with various mappings needed for analysis.
     * This constructor sets up the initial state of the analyzer with provided mappings.
     * 
     * @param candidates A map of candidate classes with their potential visitor or element types.
     * @param elementToVisitorMappings A map relating elements to their respective visitors.
     * @param subclassToSuperclassMap A map connecting subclasses to their superclasses.
     * @param methodInfo Detailed information about methods collected from the classes.
     * @param interactions Records of interactions between elements and visitors.
     */
    public VisitorAnalyzer(Map<String, Set<String>> candidates, Map<String, Set<String>> elementToVisitorMappings, Map<String, String> subclassToSuperclassMap,
            Map<String, Set<String>> methodInfo, Map<String, Set<String>> interactions) {
        this.candidates = candidates;
        this.elementToVisitorMappings = elementToVisitorMappings;
        this.subclassToSuperclassMap = subclassToSuperclassMap;
        this.methodInfo = methodInfo;
        this.interactions = interactions;
    }

    /**
     * Main analysis method that does the entire analysis process.
     * This method calls other methods in sequence to perform the analysis and print results.
     */
    public void analyze() {
        // for dev only:
        // this.log();

        this.collectPrelog();

        this.findType();

        this.verifyElementsToVisitors();

        this.printLogs();

        this.suggestions();
    }

    /**
     * Determines the visitor strategy used (single method or overloaded methods) for each visitor.
     * For each visitor, checks the number of visit methods and categorizes the strategy accordingly.
     */
    public void findType() {
        this.elementToVisitorMappings.keySet().forEach(visitor -> {
            Set<String> visitMethods = this.collectVisitMethods(visitor);

            if (visitMethods.size() == 1) {
                // System.out.println(visitor + " uses a Single Method Visitor strategy.");
                this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("uses Single Method Visitor.");
            } else {
                // System.out.println(visitor + " uses an Overloaded Methods Visitor strategy.");
                this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("uses Overloaded Methods.");
            }
        });
    }

    /**
     * Verifies if the visitors properly handle all associated elements.
     * It checks if visitors cover all expected element types and logs the analysis.
     */
    private void verifyElementsToVisitors() {
        this.elementToVisitorMappings.forEach((visitor, elements) -> {
            Set<String> allElementTypes = new HashSet<>();
            elements.forEach(element -> {
                allElementTypes.addAll(this.collectElementTypesInInheritance(element, this.constructSuperclassToSubclassesMap()));
            });
    
            // System.out.println(visitor + " should handle all of " + allElementTypes);
            this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("should handle all of " + allElementTypes);

            if (this.logsMap.get(visitor).contains("uses Single Method Visitor.")) {
                this.verifyForSingle(allElementTypes, visitor);
            } else {
                Set<String> visitMethods = this.collectVisitMethods(visitor);
                Set<String> visitMethodParamTypes = this.collectParamTypesFromMethods(visitMethods);
                this.verifyForOverloaded(visitMethodParamTypes, allElementTypes, visitor);
            }
        });
    }

    private void printLogs() {
        System.out.println("\nAnalysis Results:");
        this.logsMap.forEach((visitor, logs) -> {
            System.out.println("\n" + visitor + " behaves like a visitor:");
            if (logs.contains("uses Single Method Visitor.") || logs.contains("uses Overloaded Methods.")) {
                logs.stream().filter(log -> log.startsWith("<->")).forEach(log -> {
                    String doubleDispatchElements = log.substring(3);
                    System.out.println("\tdetects double dispatch with: " + doubleDispatchElements);
                });
                logs.stream().filter(log -> log.startsWith("uses")).forEach(log -> System.out.println("\t" + log));
                logs.stream().filter(log -> log.startsWith("should handle all of")).forEach(log -> System.out.println("\t" + log));
                logs.stream().filter(log -> !log.startsWith("<->") && !log.startsWith("uses") && !log.startsWith("should handle all of"))
                    .forEach(log -> System.out.println("\t" + log));
            }
        });
    }

    private void suggestions() {
        this.logsMap.forEach((visitor, logs) -> {
            if (!logs.contains("does not have a body.")) {
                logs.forEach(log -> {
                    if (log.startsWith("does not adequately handle all element types. It doesn't interact with: ")) {
                        System.out.println("\nFor " + visitor + ":");
                        String missingTypes = log.substring(log.indexOf(":") + 2);
                        if (!missingTypes.trim().isEmpty()) {
                            System.out.println("- It does not interact with or handle the following class(es): " + missingTypes);
                            System.out.println("Consider adding or refining visit methods to handle these element types explicitly.");
                        }
                    }
                });
        
                if (logs.contains("uses Single Method Visitor.")) {
                    System.out.println("\nFor " + visitor + ":");
                    System.out.println("- It is currently using a Single Method Visitor strategy, which might limit the flexibility and extensibility of your visitor pattern implementation.");
                    Set<String> allElementTypes = new HashSet<>();
                    if (this.elementToVisitorMappings.containsKey(visitor)) {
                        allElementTypes.addAll(this.elementToVisitorMappings.get(visitor));
                    }
                    allElementTypes.forEach(element -> {
                        System.out.println("- Consider refactoring to include a specialized visit method for " + element + " to improve clarity and maintainability.");
                    });
                } else if (logs.contains("uses Overloaded Methods.")) {
                    System.out.println("\nFor " + visitor + ":");
                    System.out.println("- Your visitor uses multiple visit methods to handle different element types, which is a good practice for clarity and flexibility.");
                }
            }
        });
    }
    
    private void verifyForOverloaded(Set<String> visitMethodParamTypes, Set<String> allElementTypes, String visitor) {
        if (visitMethodParamTypes.containsAll(allElementTypes)) {
            // System.out.println(visitor + " adequately handles all element types.");
            this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("adequately handles all element types.");
        } else {
            if (!this.hasInteraction(visitor)) {
                // System.out.println(visitor + " does not have a body");
                this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("does not have a body.");
            } else {
                // System.out.println(visitor + " does not adequately handle all element types. It doesn't interact with: " + this.findMissingElementTypes(visitMethodParamTypes, allElementTypes));
                this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("does not adequately handle all element types. It doesn't interact with: " + this.findMissingElementTypes(visitMethodParamTypes, allElementTypes));
            }
        }
    }

    private void verifyForSingle(Set<String> allElementTypes, String visitor) {
        Set<String> handledTypes = new HashSet<>();
        boolean bodyFound = false;
    
        for (String interactionKey : this.interactions.keySet()) {
            if (interactionKey.startsWith(visitor + ".")) {
                bodyFound = true;
                Set<String> interactionDetails = this.interactions.get(interactionKey);
    
                for (String detail : interactionDetails) {
                    if (detail.contains("instanceof") || detail.contains("Cast to")) {
                        String typeName = this.extractTypeNameFromDetail(detail);
                        handledTypes.add(typeName);
                    } else if (detail.contains(".")) {
                        String variableName = this.extractVariableNameBeforeDot(detail);
                        String typeName = this.mapVariableToType(visitor, variableName);
                        if (typeName != null && allElementTypes.contains(typeName)) {
                            handledTypes.add(typeName);
                        }
                    }
                }
            }
        }
    
        if (!bodyFound) {
            // System.out.println(visitor + " does not have a body");
            this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("does not have a body.");
            return;
        }
    
        if (allElementTypes.equals(handledTypes)) {
            // System.out.println(visitor + " adequately handles all expected element types with a single method.");
            this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("adequately handles all expected element types with a single method.");
        } else {
            Set<String> missingTypes = new HashSet<>(allElementTypes);
            missingTypes.removeAll(handledTypes);
            // System.out.println(visitor + " does not adequately handle all element types. It doesn't interact with: " + missingTypes);
            this.logsMap.computeIfAbsent(visitor, k -> new HashSet<>()).add("does not adequately handle all element types. It doesn't interact with: " + missingTypes);
        }
    }

    /**
     * Extracts the type name from a detail string that includes 'instanceof' or casting.
     * This utility method parses a code snippet to find and return the involved type name.
     * 
     */
    private String extractTypeNameFromDetail(String detail) {
        String typeName = null;
        if (detail.contains("instanceof")) {
            typeName = detail.split("instanceof")[1].trim().split("\\s+")[0];
        } else if (detail.contains("Cast to")) {
            typeName = detail.split("Cast to")[1].trim().split("\\s+")[0];
        }
        return typeName.replaceAll("[^a-zA-Z0-9_.$]", "");
    }
    
    private String extractVariableNameBeforeDot(String detail) {
        int dotIndex = detail.indexOf(".");
        if (dotIndex != -1) {
            String beforeDot = detail.substring(0, dotIndex).trim();
            String[] parts = beforeDot.split("\\s+");
            return parts[parts.length - 1];
        }
        return null;
    }

    private String mapVariableToType(String visitor, String variableName) {
        Set<String> methods = this.methodInfo.get(visitor);
        if (methods != null) {
            for (String method : methods) {
                int start = method.indexOf("(") + 1;
                int end = method.indexOf(")", start);
                if (start > 0 && end > start) {
                    String paramType = method.substring(start, end).trim();
                    String[] params = paramType.split(",");
                    for (String param : params) {
                        String[] typeAndName = param.trim().split("\\s+");
                        if (typeAndName[1].equals(variableName)) {
                            return typeAndName[0]; 
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private Set<String> collectParamTypesFromMethods(Set<String> visitMethods) {
        Set<String> paramTypes = new HashSet<>();
        visitMethods.forEach(method -> {
            int start = method.indexOf("(") + 1;
            int end = method.indexOf(")", start);
            if (start > 0 && end > start) {
                String paramType = method.substring(start, end).trim();
                String[] params = paramType.split(",");
                for (String param : params) {
                    String type = param.trim().split("\\s+")[0];
                    paramTypes.add(type);
                }
            }
        });
        return paramTypes;
    }
    
    private Set<String> findMissingElementTypes(Set<String> visitMethodParamTypes, Set<String> allElementTypes) {
        Set<String> missingTypes = new HashSet<>(allElementTypes);
        missingTypes.removeAll(visitMethodParamTypes);
        return missingTypes;
    }
    
    private Set<String> collectVisitMethods(String visitor) {
        Set<String> collectedMethods = new HashSet<>();
        String currentVisitor = visitor;

        while (currentVisitor != null && this.methodInfo.containsKey(currentVisitor)) {
            if (this.hasInteraction(currentVisitor)) {
                collectedMethods.addAll(this.methodInfo.get(currentVisitor));
            }
            currentVisitor = this.subclassToSuperclassMap.get(currentVisitor);
        }

        return collectedMethods;
    }

    private Map<String, Set<String>> constructSuperclassToSubclassesMap() {
        Map<String, Set<String>> superclassToSubclassesMap = new HashMap<>();
        for (Map.Entry<String, String> entry : this.subclassToSuperclassMap.entrySet()) {
            String subclass = entry.getKey();
            String superclass = entry.getValue();
            
            superclassToSubclassesMap.computeIfAbsent(superclass, k -> new HashSet<>()).add(subclass);
        }
        return superclassToSubclassesMap;
    }

    private Set<String> collectElementTypesInInheritance(String element, Map<String, Set<String>> superclassToSubclassesMap) {
        Set<String> elementTypes = new HashSet<>();
        this.collectSubclasses(this.collectSuperclasses(element, elementTypes), elementTypes, superclassToSubclassesMap);
        return this.refineElementTypes(elementTypes);
    }

    private String collectSuperclasses(String element, Set<String> elementTypes) {
        String currentElement = element;
        String superClass = element;
        while (currentElement != null) {
            superClass = currentElement;
            elementTypes.add(currentElement);
            currentElement = this.subclassToSuperclassMap.get(currentElement);
        }
        return superClass;
    }

    private void collectSubclasses(String element, Set<String> elementTypes, Map<String, Set<String>> superclassToSubclassesMap) {
        if (superclassToSubclassesMap.containsKey(element)) {
            for (String subclass : superclassToSubclassesMap.get(element)) {
                if (elementTypes.add(subclass)) {
                    this.collectSubclasses(subclass, elementTypes, superclassToSubclassesMap);
                }
            }
        }
    }

    private Set<String> refineElementTypes(Set<String> elementTypes) {
        Set<String> refinedElementTypes = new HashSet<>();
        for (String element : elementTypes) {
            if (!this.hasInteraction(element)) {
                continue;
            }
            refinedElementTypes.add(element);
        }
        return refinedElementTypes;
    }

    private boolean hasInteraction(String className) {
        for (String interactionKey : this.interactions.keySet()) {
            if (interactionKey.startsWith(className + ".")) {
                return true;
            }
        }
        return false;
    }

    private void log() {
        System.out.println("Candidates: ");
        this.candidates.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
        System.out.println("Element/Visitor Mappings: ");
        this.elementToVisitorMappings.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
        System.out.println("Subclass to Superclass Map: ");
        this.subclassToSuperclassMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
        System.out.println("Method Information: ");
        this.methodInfo.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
        System.out.println("Method interactions: ");
        this.interactions.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
    }

    private void collectPrelog() {
        this.elementToVisitorMappings.forEach((k, v) -> {
            // System.out.println("there was double dispatch detected between " + k + "<->" + v);
            this.logsMap.computeIfAbsent(k, key -> new HashSet<>()).add("<->"+v);
        });
    }

    public Map<String, Set<String>> getLogsMap() {
        return this.logsMap;
    }
}
