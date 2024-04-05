package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import java.util.List;

/**
 * A HandlerNode is a part of the chain of responsibility
 * TODO: add responsibility
 */
public class HandlerNode {
    String handlerName;
    HandlerNode next;
    List<Responsibility> responsibilities;


    public HandlerNode(String handlerName, List<Responsibility> responsibilities) {
        this.handlerName = handlerName;
        this.responsibilities = responsibilities;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setResponsibilities(List<Responsibility> responsibilities) {
        this.responsibilities = responsibilities;
    }

    public List<Responsibility> getResponsibilities() {
        return this.responsibilities;
    }

}
