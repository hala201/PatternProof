package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.example.design_pattern_verifier.service.VisitorPattern.MethodInformationExtractor;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * After the main responsibility has been parsed from the base handler
 * Collect the individual responsibilities that the handlers have and
 * add it to the HandlerNode class
 */

public class ResponsibilityCollector {

    public List<Responsibility> extractResponsibilities(ClassOrInterfaceDeclaration n, boolean isBaseHandler, List<Responsibility> baseResponsibilities) {
        if (isBaseHandler) {
            return extractBaseResponsibilities(n);
        } else {
            return extractConcreteResponsibilities(n, baseResponsibilities);
        }
    }

    /**
     * Extracts responsibilities from base handler
     * @param classOrInterface
     * @return
     */
    private List<Responsibility> extractBaseResponsibilities(ClassOrInterfaceDeclaration classOrInterface) {
        return classOrInterface.getMethods().stream()
                .map(method -> new Responsibility(
                        method.getNameAsString(),
                        method.getParameters().stream()
                                .map(p -> p.getType().asString())
                                .collect(Collectors.toList()),
                        method.getBody().orElse(new BlockStmt())
                ))
                .collect(Collectors.toList());
    }

    /**
     * Extracts concrete responsibilities that override the behavior in base handler
     * @param classOrInterface
     * @param baseResponsibilities
     * @return
     */
    private List<Responsibility> extractConcreteResponsibilities(ClassOrInterfaceDeclaration classOrInterface, List<Responsibility> baseResponsibilities) {
        return classOrInterface.getMethods().stream()
                .filter(method -> method.isAnnotationPresent(Override.class))
                .filter(method -> baseResponsibilities.stream().anyMatch(baseResp ->
                        baseResp.getMethodName().equals(method.getNameAsString()) &&
                                baseResp.getParameters().equals(method.getParameters().stream()
                                        .map(p -> p.getType().asString())
                                        .collect(Collectors.toList()))))
                .map(method -> new Responsibility(
                        method.getNameAsString(),
                        method.getParameters().stream()
                                .map(p -> p.getType().asString())
                                .collect(Collectors.toList()),
                        method.getBody().orElse(new BlockStmt())
                ))
                .collect(Collectors.toList());
    }

}
