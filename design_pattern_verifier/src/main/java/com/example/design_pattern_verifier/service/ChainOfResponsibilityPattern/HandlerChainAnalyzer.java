package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
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
    StringBuilder result;
    StringBuilder logs = new StringBuilder("");;


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
                    if (logs.toString().contains("ast1IncludesAst2") && logs.toString().contains("ast2IncludesAst1")) {
                        result.append("\n -  The implementation of the responsibility " + allResponsibilities.get(i) + " of " + parsedResponsibilities.get(allResponsibilities.get(i)) +
                                " is identical to " + allResponsibilities.get(j) + " of " + parsedResponsibilities.get(allResponsibilities.get(j)) + "!\n" +
                                "Suggestion for Chain of Responsibility Structure: \n Remove redundancy by deleting the handler " + parsedResponsibilities.get(allResponsibilities.get(j)) + " or remove its responsibility." +
                                " Otherwise, consider removing duplicate functionality from the responsibility of " + parsedResponsibilities.get(allResponsibilities.get(i)) + " to ensure handler Single Responsibility Principle.\n");
                    } else if (logs.toString().contains("ast1IncludesAst2")) {
                        result.append("\n -  The implementation of the responsibility " + allResponsibilities.get(i) + " of " + parsedResponsibilities.get(allResponsibilities.get(i)) +
                                " already covers the functionality of " + allResponsibilities.get(j) + " of " + parsedResponsibilities.get(allResponsibilities.get(j)) + "!\n" +
                                "Suggestion for Chain of Responsibility Structure: \n Remove redundancy by deleting the handler " + parsedResponsibilities.get(allResponsibilities.get(j)) + " or remove its responsibility." +
                                " Otherwise, consider removing duplicate functionality from the responsibility of " + parsedResponsibilities.get(allResponsibilities.get(i)) + " to ensure handler Single Responsibility Principle.\n");
                    } else if (logs.toString().contains("ast2IncludesAst1")) {
                        result.append("\n -  The implementation of the responsibility " + allResponsibilities.get(j) + " of " + parsedResponsibilities.get(allResponsibilities.get(j)) +
                                " already covers the functionality of " + allResponsibilities.get(i) + " of " + parsedResponsibilities.get(allResponsibilities.get(i)) + "!\n" +
                                "Suggestion for Chain of Responsibility Structure: \n Remove redundancy by deleting the handler " + parsedResponsibilities.get(allResponsibilities.get(i)) + " or remove its responsibility." +
                                " Otherwise, consider removing duplicate functionality from the responsibility of " + parsedResponsibilities.get(allResponsibilities.get(j)) + " to ensure handler Single Responsibility Principle.\n");
                    }
                    result.append("\n");

                    result.append("\n ===> Redundancy Detected: \n The handlers " + parsedResponsibilities.get(allResponsibilities.get(i)) + " and "
                            + parsedResponsibilities.get(allResponsibilities.get(j)) +
                            " have a redundancy in their responsibility + " + allResponsibilities.get(i) + "\n");

                    result.append("\n");
                }
            }
        }
    }

    private void logChainAnalysis() {
        result = new StringBuilder("Chain of Responsibility Analysis Results for Chain Structure:  \n");

        result.append("\n");

        result.append("\nPlease evaluate if this is your intended structure since our tool can't tell if this was precisely " +
                "your intended design. We can only approximate and detect certain redundancies in your handler structure.\n");

        result.append("\n");

        result.append("Chain: handler to handler:\n");
        for (String handler: chain.getHandlerNames()) {
            result.append("->" + handler);
        }
        result.append("\n");
        result.append("\n Handler hierarchy (ConcreteHandlerClass=BaseHandlerClass): \n" + handlerHierarchy + "\n");
        for (String handler: handlerHierarchy.keySet()) {
            if(!chainObjects.values().contains(handler)) {
                result.append("\n ===> Redundancy Detected:Handler " + handler + " is redundant since it is implemented but not used in the chain\n");
            }
        }
        result.append("\n");

        result.append("\nBase Handler(s): " + baseHandlers + "\n");
        result.append("\nConcrete Handlers: " + concreteResponsibilitiesMap.keySet() + "\n");
        result.append("\nResponsibilities extracted from base handler: " + responsibilitiesMap.values() + "\n");

        result.append("\n");

        result.append("\nClient(s): " + clients + "\n");

        result.append("\n");
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

        if (isSetNextCall(ast1) || isSetNextCall(ast2)) return false;

        Set<String> statementsAst1 = collectStatements(ast1);
        Set<String> statementsAst2 = collectStatements(ast2);

        // If one AST is a subset of the other
        boolean ast1IncludesAst2 = statementsAst2.containsAll(statementsAst1);
        boolean ast2IncludesAst1 = statementsAst1.containsAll(statementsAst2);

        // If one AST calls a function that is a subset of the other
        boolean ast1CallsMethodWithBodyOfR2 = callsMethodWithBody(ast1, r2.getMethodBody());
        boolean ast2CallsMethodWithBodyOfR1 = callsMethodWithBody(ast2, r1.getMethodBody());

        if (ast1IncludesAst2 || ast1CallsMethodWithBodyOfR2) {
            logs.append("ast1IncludesAst2");

        }
        if (ast2IncludesAst1 || ast2CallsMethodWithBodyOfR1) {
            logs.append("ast2IncludesAst1");
        }

        return ast1IncludesAst2 || ast2IncludesAst1 || ast1CallsMethodWithBodyOfR2 || ast2CallsMethodWithBodyOfR1;
    }

    private Set<String> collectStatements(BlockStmt ast) {
        Set<String> statements = new HashSet<>();
        ast.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(BlockStmt n, Void arg) {
                for (Statement stmt : n.getStatements()) {
                    statements.add(stmt.toString());
                }
                super.visit(n, arg);
            }
        }, null);
        return statements;
    }


    private boolean callsMethodWithBody(BlockStmt ast, BlockStmt otherBody) {
        final boolean[] callsMethodWithBody = {false};
        ast.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodCallExpr n, Void arg) {
                String methodName = n.getNameAsString();
                BlockStmt methodBody = getMethodBodyByNameOfHelperInBaseHandler(methodName);
                if (methodBody != null && methodBody.equals(otherBody)) {
                    callsMethodWithBody[0] = true;
                }
                super.visit(n, arg);
            }
        }, null);
        return callsMethodWithBody[0];
    }

    private BlockStmt getMethodBodyByNameOfHelperInBaseHandler(String methodName) {
        List<Responsibility> possibleDeclaredFunctions = responsibilitiesMap.get(baseHandlers.get(0));
        for (Responsibility r: possibleDeclaredFunctions) {
            if(r.getMethodName().equals(methodName)) {
                return r.getMethodBody();
            }
        }
        return null;
    }

    private boolean isSetNextCall(BlockStmt ast) {
        final boolean[] isSetNext = {false};
        ast.accept(new VoidVisitorAdapter<Void>() {
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
        return isSetNext[0];
    }


    public boolean isHandler(String name) {
        return (baseHandlers.contains(name) || handlerHierarchy.containsKey(name));
    }

    public String getFormattedAnalysisResults() {
        return result.toString();
    }
}
