package com.example.design_pattern_verifier.ChainOfResponsibility;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Chain;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.HandlerNode;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Responsibility;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChainTest {

    @Test
    void testAddHandler() {
        Chain chain = new Chain();
        HandlerNode handlerNode = new HandlerNode("Handler1", null);
        chain.addHandler("Handler1", null);

        assertEquals(1, chain.getHandlerNames().size());
        assertEquals(handlerNode.getHandlerName(), chain.getHandlerNames().get(0));
    }

    @Test
    void testGetHandlerNames() {
        Chain chain = new Chain();
        List<Responsibility> handler1Responsibilities =  new ArrayList<>();
        handler1Responsibilities.add(new Responsibility("doSomething", null, null));

        List<Responsibility> handler2Responsibilities =  new ArrayList<>();
        handler1Responsibilities.add(new Responsibility("doSomethingElse", null, null));


        chain.addHandler("Handler1", handler1Responsibilities);
        chain.addHandler("Handler2", handler2Responsibilities);

        assertTrue(chain.getHandlerNames().contains("Handler1"));
        assertTrue(chain.getHandlerNames().contains("Handler2"));
    }
}