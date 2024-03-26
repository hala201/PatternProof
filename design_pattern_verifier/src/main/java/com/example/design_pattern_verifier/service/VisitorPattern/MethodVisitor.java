package com.example.design_pattern_verifier.service.VisitorPattern;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.NodeList;

import java.util.HashSet;
import java.util.Set;

public class MethodVisitor extends VoidVisitorAdapter<Void> {
    private Set<String> types = new HashSet<>();
    public MethodVisitor() {

    }

    public Set<String> getUsedTypes() {
        return types;
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        // search for main function
        if (n.getName().asString().equals("main")) {
            n.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(VariableDeclarationExpr n, Void arg) {
                    NodeList<VariableDeclarator> variables = n.getVariables();
                    for (VariableDeclarator var : variables) {
                        types.add(var.getType().toString());
                    }
                    super.visit(n, arg);
                }

                @Override
                public void visit(ObjectCreationExpr n, Void arg) {
                    types.add(n.getType().asString());
                    super.visit(n, arg);
                }
            }, null);
        }
        super.visit(n, arg);
    }
}
