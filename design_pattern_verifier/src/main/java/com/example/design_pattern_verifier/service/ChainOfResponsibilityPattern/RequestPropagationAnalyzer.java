package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.stmt.*;

import java.util.*;

public class RequestPropagationAnalyzer {
    private final Map<String, String> handlerHierarchy;
    private final List<String> baseHandlers;

    private Map<String, List<Responsibility>> baseHandlerResponsibilities;
    private final Chain chain;

    private final Map<String, String> chainVariables;
    private final Map<String, String> chainObjects;
    private final List<String> clients;
    private final Set<String> requestMethods;

    private final Map<String, String> canPropagate = new HashMap<>();

    private final Map<String, String> cannotPropagate = new HashMap<>();

    private final boolean circularChain;

    private StringBuilder result;

    public RequestPropagationAnalyzer(
            Map<String, String> handlerHierarchy,
            List<String> baseHandlers,
            Map<String, List<Responsibility>> baseHandlerResponsibilities,
            Chain chain,
            Map<String, String> chainVariables,
            Map<String, String> chainObjects,
            List<String> clients,
            Set<String> handlerMethods,
            boolean circularChain) {
        this.handlerHierarchy = handlerHierarchy;
        this.baseHandlers = baseHandlers;
        this.baseHandlerResponsibilities = baseHandlerResponsibilities;
        this.chain = chain;
        this.clients = clients;
        this.chainVariables = chainVariables;
        this.chainObjects = chainObjects;
        this.requestMethods = handlerMethods;
        this.circularChain = circularChain;

    }

    public void analyze() {
        result = new StringBuilder("Request Propagation Results for client: " + this.clients + "\n");
        result.append("\t - client calls the following request methods: ").append(requestMethods).append("\n");
        if (circularChain) {
            result.append("\t - the created chain is circular which can result in an endless loop of requests. \n");
            System.out.println();
        } else {
            checkPropagation();
            if (!canPropagate.isEmpty()) {
                result.append("\t - the following chain handlers have the ability to propagate requests to handlers down the chain: ").append(canPropagate).append("\n");
            }
            if (!cannotPropagate.isEmpty()) {
                result.append("\t - the following chain handlers cannot propagate requests to the handlers down the chain: ").append(cannotPropagate).append("\n");
                result.append("\nSuggestion: Ensure that implemented request methods in the following handlers can propagates to the next handler in chain.");
                result.append("\nHandlers to investigate:\n");
                for (String handler: cannotPropagate.keySet()) {
                    result.append("\t - Handler of class type ").append(chainObjects.get(handler)).append(" with request method ").append(cannotPropagate.get(handler)).append("\n");
                }
            }
        }
    }

    private void checkPropagation() {
        for (String handler: chain.getHandlerNames()) {
            for (String request: requestMethods) {
                BlockStmt block = findRequest(request, handler);

                final Boolean[] canPropegate = {false};
                if (block != null) {
                    block.accept(new VoidVisitorAdapter<Void>() {
                        @Override
                        public void visit(MethodCallExpr n, Void arg) {
                            if (n.getScope().isPresent() &&
                                n.getNameAsString().equals(request) &&
                                baseHandlers.contains(
                                        n.getScope().get().calculateResolvedType().asReferenceType().getQualifiedName())) {
                                canPropagate.put(handler, request);
                                canPropegate[0] = true;
                            }
                        }
                    }, null);
                }
                if (!canPropegate[0] && !chain.tail.handlerName.equals(handler)) {
                    cannotPropagate.put(handler, request);
                }
            }
        }
    }

    private BlockStmt findRequest(String request, String handler) {
        if ( chain.getConcreteHandlerResponsibilityMap().containsKey(handler)) {
            for (Responsibility responsibility:  chain.getConcreteHandlerResponsibilityMap().get(handler)) {
                if (responsibility.getMethodName().equals(request)) {
                    return responsibility.getMethodBody();
                }
            }
        }

        if (baseHandlerResponsibilities.containsKey(handlerHierarchy.get(chainObjects.get(handler)))) {
            for (Responsibility responsibility: baseHandlerResponsibilities.get(handlerHierarchy.get(chainObjects.get(handler)))) {
                if (responsibility.getMethodName().equals(request)) {
                    return responsibility.getMethodBody();
                }
            }
        }

        return null;
    }

    public String getFormattedAnalysisResults() {
        return result.toString();
    }
}