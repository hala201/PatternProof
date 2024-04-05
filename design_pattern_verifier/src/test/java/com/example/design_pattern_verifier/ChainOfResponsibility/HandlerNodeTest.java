package com.example.design_pattern_verifier.ChainOfResponsibility;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.HandlerNode;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Responsibility;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandlerNodeTest {

    @Test
    void testGetHandlerName() {
        HandlerNode handlerNode = new HandlerNode("Handler1", null);
        assertEquals("Handler1", handlerNode.getHandlerName());
    }

    @Test
    void testGetResponsibilities() {
        HandlerNode handlerNode = new HandlerNode("Handler1", null);
        Responsibility responsibility = new Responsibility("write", null, null);
        List<Responsibility> responsibilities = new ArrayList<>();
        responsibilities.add(responsibility);
        handlerNode.setResponsibilities(responsibilities);

        assertEquals(1, handlerNode.getResponsibilities().size());
        assertEquals(responsibility, handlerNode.getResponsibilities().get(0));
    }
}
