package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.github.javaparser.ast.stmt.BlockStmt;

import java.util.*;

public class HandlerChainAnalyzer {
    private final Map<String, String> handlerHierarchy;
    private final List<String> baseHandlers;
    private final Chain chain;
    private final List<String> clients;
    private final Map<String, List<Responsibility>> responsibilitiesMap;
    private final Map<String, List<Responsibility>> concreteResponsibilitiesMap;


    public HandlerChainAnalyzer(
            Map<String, String> handlerHierarchy, List<String> baseHandlers, Chain chain, List<String> clients, Map<String, List<Responsibility>> responsibilitiesMap, Map<String, List<Responsibility>> concreteResponsibilitiesMap) {
        this.handlerHierarchy = handlerHierarchy;
        this.baseHandlers = baseHandlers;
        this.chain = chain;
        this.clients = clients;
        this.responsibilitiesMap = responsibilitiesMap;
        this.concreteResponsibilitiesMap = concreteResponsibilitiesMap;
    }

    public void analyze() {
        /**
         * This analysis checks the following:
         * I. Base Handler:
         * 1. base handler should have a class field of its own type
         * 2. uses that class field in a method otherwise it might NOT be a proper chain
         * II. Handlers:
         * 1. Each handler should have some responsibility of its own
         * 2. Each handler should have a unique responsibility (look at methods marked with @Override)
         *
         * Produce a map of each handler and its responsibility and analyze that.
         */
        logChainAnalysis();

        Map<Responsibility, String> parsedResponsibilities = new HashMap<>();
        for (Map.Entry<String, List<Responsibility>> entry: concreteResponsibilitiesMap.entrySet()) {
            for (Responsibility r: entry.getValue()) {
                parsedResponsibilities.put(r, entry.getKey());
            }
        }

        List<Responsibility> allResponsibilities = new ArrayList<>(parsedResponsibilities.keySet());
        for (int i = 0; i < allResponsibilities.size(); i++) {
            for (int j = i+1; j < allResponsibilities.size(); j++) {
                if (compareResponsibilities(allResponsibilities.get(i), allResponsibilities.get(j))){
                    System.out.println("Handlers " + parsedResponsibilities.get(allResponsibilities.get(i)) + " and "
                            + parsedResponsibilities.get(allResponsibilities.get(i)) +
                            " have at least one equal responsibility, refactor for redundancy");
                }
            }
        }
    }

    private void logChainAnalysis() {
        System.out.println("Chain of Handlers: ");
        for (String handler: chain.getHandlerNames()) {
            System.out.println("->" + handler);
        }
        System.out.println("Handler hierarchy: " + handlerHierarchy);
        System.out.println("Base Handler(s): " + baseHandlers);
        System.out.println("Client(s): " + clients);
        System.out.println("Responsibility: " + responsibilitiesMap);
    }

    /**
     * Basic AST comparison catches if ASTs are identical
     * TODO: (HALA) will improve to look through the control flow
     * @param r1
     * @param r2
     * @return
     */
    private boolean compareResponsibilities(Responsibility r1, Responsibility r2) {
        BlockStmt ast1 = r1.getMethodBody();
        BlockStmt ast2 = r2.getMethodBody();

        return ast1.equals(ast2);
    }
}
