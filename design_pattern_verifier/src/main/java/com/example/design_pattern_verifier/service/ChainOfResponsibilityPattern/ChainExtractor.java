package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.example.design_pattern_verifier.service.VisitorPattern.MethodInformationExtractor;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The chain extractor assumes the following elements:
 * 1. Base Handler: an abstract class/interface that handlers extend/implement
 * 2. Handlers: extend/implement the base handler
 * 3. Client: the runnable class
 *
 *  */

public class ChainExtractor extends VoidVisitorAdapter<Void> {

    private Map<String, String> handlerHierarchy = new HashMap<>();
    private List<String> baseHandlers = new ArrayList<>();
    private Chain chain = new Chain();

    private Map<String, List<Responsibility>> potentialHandlerNodes = new HashMap<>();
    private List<String> clients = new ArrayList<>();

    private Map<String, List<Responsibility>> baseHandlerResponsibilities = new HashMap<>();

    private MethodInformationExtractor methodInformationExtractor = new MethodInformationExtractor();
    private Set<String> confirmedBaseHandlers = new HashSet<>();
    private ResponsibilityCollector responsibilityCollector = new ResponsibilityCollector();

    private boolean circularChain = false;
    private final Map<String, String> chainVariables = new HashMap<>();
    private final Map<String, String> chainObjects = new HashMap<>();

    private final Map<String, String> handlerToHandler = new HashMap<>();
    private final Set<String> requestMethods = new HashSet<>();

    /**
     * 1. Base Handler: an abstract class/interface that handlers extend/implement
     * 2. Handlers: extend/implement the base handler
     * 3. Client: the runnable class
     *  TODO: (DYLAN) make sure that client is the one that makes requests
     *  TODO: (DYLAN) make sure that the chain is reordered according to the sequence of requests
     * @param n
     * @param arg
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        n.getExtendedTypes().forEach(ext -> {
            String handler = n.getNameAsString();
            String baseHandler = ext.getNameAsString();
            this.handlerHierarchy.put(handler, baseHandler);
            if (!baseHandlers.contains(baseHandler)) {
                baseHandlers.add(baseHandler);
            }
        });
        n.getImplementedTypes().forEach(impl -> {
            String handler = n.getNameAsString();
            String baseHandler = impl.getNameAsString();
            this.handlerHierarchy.put(handler, baseHandler);
            if (!baseHandlers.contains(baseHandler)) baseHandlers.add(baseHandler);
        });

        n.getMethods().forEach(method -> {
            String className = n.getNameAsString();
            String methodSignature = methodInformationExtractor.extractMethodSignature(method);
            if (methodSignature.contains("main")) clients.add(className);
        });

        super.visit(n, arg);

    }
    @Override
    public void visit(CompilationUnit cu, Void arg) {
        super.visit(cu, arg);

        // Base Handler
        confirmedBaseHandlers = baseHandlers.stream()
                .filter(baseHandler -> !handlerHierarchy.containsKey(baseHandler))
                .collect(Collectors.toSet());
        extractBaseResponsibilities(cu);
        extractConcreteResponsibilities(cu);

        // Chain
        cu.accept(new ClientVisitor(), arg);
        createChain();

        findRequestMethods();
    }

    private class ClientVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            // Find the clients which contain the chain
            if (clients.contains(n.getNameAsString())) {
                n.accept(new VoidVisitorAdapter<Void>() {
                    // Get field declarations
                    @Override
                    public void visit(FieldDeclaration n, Void arg) {
                        processVariables(n.getVariables());
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(VariableDeclarationExpr n, Void arg) {
                        NodeList<VariableDeclarator> variables = n.getVariables();
                        processVariables(n.getVariables());
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(AssignExpr n, Void arg) {
                        if (n.getOperator() == AssignExpr.Operator.ASSIGN && n.getValue().isObjectCreationExpr()){
                            ObjectCreationExpr object = n.getValue().asObjectCreationExpr();

                            chainObjects.put(n.getTarget().toString(), object.getTypeAsString());
                        }
                        super.visit(n, arg);
                    }

                    @Override
                    public void visit(MethodCallExpr n, Void arg) {

                        // Finding setNext calls without depending on "setNext being in method name"
                        final Boolean[] isSetNext = {false};
                        if (n.getScope().isPresent() &&
                                n.getArguments().isNonEmpty() &&
                                !n.getArgument(0).calculateResolvedType().isPrimitive()){
                            String resolved_type = n.getScope().get().calculateResolvedType().asReferenceType().getQualifiedName();
                            String argument_type = n.getArgument(0).calculateResolvedType().asReferenceType().getQualifiedName();
                            String resolved_name = n.getScope().get().toString();

                            String call_type = chainObjects.get(resolved_name);

                            if ((isHandler(call_type) || isHandler(resolved_type)) && isHandler(argument_type)) {
                                BlockStmt block = getResponsibilityBody(call_type, n.getNameAsString());
                                if (block == null) {
                                    block = getResponsibilityBody(resolved_type, n.getNameAsString());
                                }

                                if (block != null) {

                                    block.accept(new VoidVisitorAdapter<Void>() {
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
                                }
                            }
                        }

                        if (isSetNext[0]) {
                            handlerToHandler.put(n.getScope().get().toString(), n.getArgument(0).toString());
                        }

                        super.visit(n, arg);
                    }
                }, null);
            }
        }
    }

    public BlockStmt getResponsibilityBody(String handler, String method) {
        if (baseHandlerResponsibilities.containsKey(handler)) {
            for (Responsibility responsibility: baseHandlerResponsibilities.get(handler)) {
                if (responsibility.getMethodName().equals(method)) {
                    return responsibility.getMethodBody();
                }
            }
        }
        if (potentialHandlerNodes.containsKey(handler)) {
            for (Responsibility responsibility: potentialHandlerNodes.get(handler)) {
                if (responsibility.getMethodName().equals(method)) {
                    return responsibility.getMethodBody();
                }
            }
        }

        return null;
    }

    public boolean isHandler(String name) {
        return (confirmedBaseHandlers.contains(name) || handlerHierarchy.containsKey(name));
    }

    private void extractConcreteResponsibilities(CompilationUnit cu) {
        cu.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration n = (ClassOrInterfaceDeclaration) type;
                if (!confirmedBaseHandlers.contains(n.getNameAsString())) {
                    potentialHandlerNodes.put(
                            n.getNameAsString(),
                            responsibilityCollector.extractResponsibilities(n,
                                            false,
                                            baseHandlerResponsibilities.get(
                                                    handlerHierarchy.get(n.getNameAsString()))));
                }
            }
        });
    }

    private void extractBaseResponsibilities(CompilationUnit cu) {
        cu.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration n = (ClassOrInterfaceDeclaration) type;
                if (confirmedBaseHandlers.contains(n.getNameAsString())) {
                    baseHandlerResponsibilities.put(n.getNameAsString(),
                            responsibilityCollector.extractResponsibilities(n, true, null));
                }
            }
        });
    }

    private void findRequestMethods() {
       findRequestMethodsHelper(baseHandlerResponsibilities.values());
       findRequestMethodsHelper(chain.getConcreteHandlerResponsibilityMap().values());
    }

    private void findRequestMethodsHelper(Collection<List<Responsibility>> collection) {
        for (List<Responsibility> responsibilities: collection) {
            for (Responsibility responsibility : responsibilities) {
                BlockStmt block = responsibility.getMethodBody();
                block.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(MethodCallExpr n, Void arg) {
                        if (n.getNameAsString().equals(responsibility.getMethodName()) &&
                                n.getScope().isPresent() &&
                                confirmedBaseHandlers.contains(
                                        n.getScope().get().calculateResolvedType().asReferenceType().getQualifiedName())) {
                                requestMethods.add(responsibility.getMethodName());
                        }
                    }
                }, null);
            }
        }
    }

    private void processVariables(NodeList<VariableDeclarator> variables) {
        for (VariableDeclarator var : variables) {
            // Find creation of handler objects
            if (handlerHierarchy.containsKey(var.getTypeAsString()) ||
                    confirmedBaseHandlers.contains(var.getTypeAsString())) {
                chainVariables.put(var.getNameAsString(), var.getTypeAsString());

                Optional<Expression> exp = var.getInitializer();
                if (exp.isPresent() && exp.get().isObjectCreationExpr()) {
                    if (!chainObjects.containsKey(var.getNameAsString()) ||
                            (chainObjects.containsKey(var.getNameAsString()) &&
                                    chainObjects.get(var.getNameAsString()).equals("Empty"))) {
                        chainObjects.put(var.getNameAsString(),
                                exp.get().asObjectCreationExpr().getTypeAsString());
                    } else {
                        chainObjects.put(var.getNameAsString(), "Empty");
                    }
                }
            }
        }
    }

    private void createChain() {
        Set<String> uninitializedChainObjects = new HashSet<>();
        // Ensure each chain variable has an associated chain Object
        for (String variable: chainVariables.keySet()) {
            if (!chainObjects.containsKey(variable)) {
                uninitializedChainObjects.add(variable);
            }
        }

        String current = "head";
        for (String key: handlerToHandler.keySet()) {
            if (!handlerToHandler.containsValue(key)) {
                current = key;
            }
        }

        if (current.equals("head")) {
            //System.out.println("Chain is circular");
            this.circularChain = true;
        }

        while(!handlerToHandler.isEmpty() && !circularChain) {
            chain.addHandler(current, potentialHandlerNodes.get(chainObjects.get(current)));
            String temp = current;
            current = handlerToHandler.get(current);
            handlerToHandler.remove(temp);

            if (handlerToHandler.isEmpty()) {
                chain.addHandler(current, potentialHandlerNodes.get(chainObjects.get(current)));
            }
        }
    }

    public Map<String, List<Responsibility>> getBaseHandlerResponsibilities() {
        return baseHandlerResponsibilities;
    }

    public Map<String, String> getHandlerHierarchy() {
        return handlerHierarchy;
    }

    public List<String> getBaseHandlers() {
        return baseHandlers;
    }

    public Chain getChain() {
        return chain;
    }

    public List<String> getClients() {
        return clients;
    }

    public Map<String, String> getChainVariables() {
        return chainVariables;
    }

    public Map<String, String> getChainObjects() {
        return chainObjects;
    }

    public Map<String, String> getHandlerToHandler() {
        return handlerToHandler;
    }

    public Set<String> getRequestMethods() {
        return requestMethods;
    }

    public boolean isCircularChain() {
        return this.circularChain;
    }
}