package com.example.design_pattern_verifier.ChainOfResponsibility;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Chain;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.HandlerChainAnalyzer;
import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Responsibility;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HandlerChainAnalyzerTest {

    private HandlerChainAnalyzer analyzer;
    private Map<String, String> handlerHierarchy;
    private List<String> baseHandlers;
    private Chain chain;
    private List<String> clients;
    private Map<String, List<Responsibility>> responsibilitiesMap;
    private Map<String, List<Responsibility>> concreteResponsibilitiesMap;
    private Map<String, String> chainObjects;

    @BeforeEach
    void setUp() {
        handlerHierarchy = new HashMap<>();
        baseHandlers = new ArrayList<>();
        chain = new Chain();
        clients = new ArrayList<>();
        responsibilitiesMap = new HashMap<>();
        concreteResponsibilitiesMap = new HashMap<>();
        chainObjects = new HashMap<>();

        handlerHierarchy.put("ConcreteHandler", "BaseHandler");
        baseHandlers.add("BaseHandler");

        Responsibility responsibility = new Responsibility("handle", null, new BlockStmt());
        List<Responsibility> responsibilities = new ArrayList<>();
        responsibilities.add(responsibility);
        concreteResponsibilitiesMap.put("ConcreteHandler", responsibilities);

        analyzer = new HandlerChainAnalyzer(handlerHierarchy, baseHandlers, chain, clients, responsibilitiesMap, concreteResponsibilitiesMap, chainObjects);
    }

    @Test
    void testAnalyze() {
        assertDoesNotThrow(() -> analyzer.analyze());
    }

    @Test
    void testIsHandler() {
        assertTrue(analyzer.isHandler("BaseHandler"));
        assertTrue(analyzer.isHandler("ConcreteHandler"));
        assertFalse(analyzer.isHandler("NonExistentHandler"));
    }

}