package com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that represents the chain of handlers
 * that follow the Chain of Responsibility design pattern.
 * The chain is represented through a doubly linked list.
 */
public class Chain {
    HandlerNode head;
    HandlerNode tail;
    List<String> handlerNames;

    public Chain() {
        head = null;
        tail = null;
        handlerNames = new ArrayList<>();
    }

    public void addHandler(String handlerName, List<Responsibility> responsibility)
    {
        HandlerNode temp = new HandlerNode(handlerName, responsibility);
        if (head == null) {
            head = temp;
        }
        else {
            tail.next = temp;
        }
        tail = temp;
        handlerNames.add(handlerName);
    }

    public boolean contains(String handlerName)
    {
        HandlerNode current = head;
        while (current != null) {
            current = current.next;
            if (current.handlerName == handlerName) return true;
        }
        return false;
    }

    public List<String> getHandlerNames() {
        return handlerNames;
    }

    public Map<String, List<Responsibility>> getConcreteHandlerResponsibilityMap() {
        Map<String, List<Responsibility>> concreteHandlerResponsibilityMap = new HashMap<>();
        HandlerNode current = head;
        while (current != null) {
            concreteHandlerResponsibilityMap.put(current.handlerName, current.responsibilities);
            current = current.next;
        }
        return concreteHandlerResponsibilityMap;
    }
}