package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.example.design_pattern_verifier.service.VisitorPattern.MethodInformationExtractor;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
    private List<String> clients = new ArrayList<>();
    private Map<String, List<Responsibility>> baseHandlerResponsibilities = new HashMap<>();

    private MethodInformationExtractor methodInformationExtractor = new MethodInformationExtractor();
    private Set<String> confirmedBaseHandlers = new HashSet<>();
    private ResponsibilityCollector responsibilityCollector = new ResponsibilityCollector();


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
        confirmedBaseHandlers = baseHandlers.stream()
                .filter(baseHandler -> !handlerHierarchy.containsKey(baseHandler))
                .collect(Collectors.toSet());
        extractBaseResponsibilities(cu);
        extractConcreteResponsibilities(cu);
    }

    private void extractConcreteResponsibilities(CompilationUnit cu) {
        cu.getTypes().forEach(type -> {
            if (type instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration n = (ClassOrInterfaceDeclaration) type;
                if (!confirmedBaseHandlers.contains(n.getNameAsString())) {
                    chain.addHandler(n.getNameAsString(),
                            responsibilityCollector.extractResponsibilities(n,
                                    false,
                                    baseHandlerResponsibilities.get(handlerHierarchy.get(n.getNameAsString()))));
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
}