package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import com.github.javaparser.ast.stmt.BlockStmt;

import java.util.List;

public class Responsibility {
    private String methodName;
    private List<String> parameters;
    private BlockStmt methodBody;

    public Responsibility(String methodName, List<String> parameters, BlockStmt methodBody) {
        this.methodName = methodName;
        this.parameters = parameters;
        this.methodBody = methodBody;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public BlockStmt getMethodBody() {
        return methodBody;
    }

    @Override
    public String toString() {
        return methodName + "(" + String.join(", ", parameters) + ")";
    }
}
