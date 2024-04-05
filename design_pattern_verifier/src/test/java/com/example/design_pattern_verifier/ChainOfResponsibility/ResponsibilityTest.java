package com.example.design_pattern_verifier.ChainOfResponsibility;

import com.example.design_pattern_verifier.service.ChainOfResponsibilityPattern.Responsibility;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResponsibilityTest {

    @Test
    void testGetMethodName() {
        Responsibility responsibility = new Responsibility("write", null, new BlockStmt());
        assertEquals("write", responsibility.getMethodName());
    }

    @Test
    void testGetMethodBody() {
        BlockStmt blockStmt = new BlockStmt();
        Responsibility responsibility = new Responsibility("write", null, blockStmt);
        assertEquals(blockStmt, responsibility.getMethodBody());
    }
}
