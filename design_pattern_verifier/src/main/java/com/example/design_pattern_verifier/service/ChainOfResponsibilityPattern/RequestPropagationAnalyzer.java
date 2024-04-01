package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.stmt.*;

import java.util.*;

public class RequestPropagationAnalyzer {
    private final Map<String, String> handlerHierarchy;
    private final List<String> baseHandlers;
    private final Chain chain;
    private final List<String> clients;
    private final Set<String> requestMethods;

    private final Map<String, String> canPropagate = new HashMap<>();

    private final Map<String, String> cannotPropagate = new HashMap<>();

    private final boolean circularChain;

    private StringBuilder result;

    public RequestPropagationAnalyzer(
            Map<String, String> handlerHierarchy,
            List<String> baseHandlers,
            Chain chain,
            List<String> clients,
            Set<String> handlerMethods,
            boolean circularChain) {
        this.handlerHierarchy = handlerHierarchy;
        this.baseHandlers = baseHandlers;
        this.chain = chain;
        this.clients = clients;
        this.requestMethods = handlerMethods;
        this.circularChain = circularChain;

    }

    public void analyze() {
        result = new StringBuilder("Request Propagation Results: \n");
        result.append("Requeset Methods: " + requestMethods + "\n");
        if (circularChain) {
            result.append("Chain is circular: possible endless loop \n");
            System.out.println();
        } else {
            checkPropagation();
            result.append("Handlers that can possibly propagate: " + canPropagate + "\n");
            result.append("Handlers that cannot propagate: " + cannotPropagate + "\n");
        }
    }

    private void checkPropagation() {

        for (String handler: chain.getHandlerNames()) {

            List<Responsibility>  responsibilities = chain.getConcreteHandlerResponsibilityMap().get(handler);
            for (String request: requestMethods) {
                BlockStmt block = findRequest(request, responsibilities);

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

    private BlockStmt findRequest(String request, List<Responsibility> responsibilities) {
        for (Responsibility responsibility: responsibilities) {
            if (responsibility.getMethodName().equals(request)) {
                return responsibility.getMethodBody();
            }
        }

        return null;
    }

    public String getFormattedAnalysisResults() {
        return result.toString();
    }
}