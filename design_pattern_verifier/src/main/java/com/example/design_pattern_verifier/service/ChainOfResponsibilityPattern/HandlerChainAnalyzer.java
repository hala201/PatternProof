package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

public class HandlerChainAnalyzer {
    private final Map<String, String> handlerHierarchy;
    private final List<String> baseHandlers;
    private final Chain chain;
    private final List<String> clients;
    private final Map<String, List<Responsibility>> responsibilitiesMap;
    private final Map<String, List<Responsibility>> concreteResponsibilitiesMap;
    private final Map<String, String> chainObjects;


    public HandlerChainAnalyzer(
            Map<String, String> handlerHierarchy, List<String> baseHandlers, Chain chain, List<String> clients, Map<String, List<Responsibility>> responsibilitiesMap, Map<String, List<Responsibility>> concreteResponsibilitiesMap, Map<String, String> chainObjects) {
        this.handlerHierarchy = handlerHierarchy;
        this.baseHandlers = baseHandlers;
        this.chain = chain;
        this.clients = clients;
        this.responsibilitiesMap = responsibilitiesMap;
        this.concreteResponsibilitiesMap = concreteResponsibilitiesMap;
        this.chainObjects = chainObjects;
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
                            + parsedResponsibilities.get(allResponsibilities.get(j)) +
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
        for (String handler: handlerHierarchy.keySet()) {
            if(!chainObjects.values().contains(handler)) {
                System.out.println("Handler " + handler + " is redundant since it is declared but not used in the chain");
            }
        }

        System.out.println("Base Handler(s): " + baseHandlers);
        System.out.println("Client(s): " + clients);
        System.out.println("Responsibility: " + responsibilitiesMap);
    }

    /**
     * Basic AST comparison catches if ASTs are identical
     * @param r1
     * @param r2
     * @return
     */
    private boolean compareResponsibilities(Responsibility r1, Responsibility r2) {
        BlockStmt ast1 = r1.getMethodBody();
        BlockStmt ast2 = r2.getMethodBody();

        final boolean[] isSetNext = {false};
        ast1.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(AssignExpr n, Void arg) {
                if (n.getOperator() == AssignExpr.Operator.ASSIGN){
                    String target = n.getTarget().calculateResolvedType().asReferenceType().getQualifiedName();
                    String value = n.getValue().calculateResolvedType().asReferenceType().getQualifiedName();
                    if (isHandler(target) && isHandler(value)) {
                        isSetNext[0] = true;
                    }
                }
            }
        }, null);
        if (isSetNext[0]) return false;
        return ast1.equals(ast2);
    }

    public boolean isHandler(String name) {
        return (baseHandlers.contains(name) || handlerHierarchy.containsKey(name));
    }
}
