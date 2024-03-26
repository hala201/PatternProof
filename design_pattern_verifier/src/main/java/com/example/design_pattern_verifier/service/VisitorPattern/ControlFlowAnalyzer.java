package com.example.design_pattern_verifier.service.VisitorPattern;

import java.util.HashSet;
import java.util.Set;

public class ControlFlowAnalyzer {
    private Set<String> visType;
    private Set<String> typeUsed;
    public ControlFlowAnalyzer(Set<String> typeUsed, Set<String> visType) {
        this.visType = visType;
        this.typeUsed = typeUsed;
    }
    public void analyze() {
        if (!this.typeUsed.isEmpty()) {
            System.out.println("Data types found in main method: " + this.typeUsed);
            Set<String> UsedType = new HashSet<>();
            Set<String> NonUsedType = new HashSet<>();
            for (String type : this.visType) {
                if (this.typeUsed.contains(type))
                {
                    UsedType.add(type);
                }
                else
                {
                    NonUsedType.add(type);
                }
            }
            System.out.println("[control flow] Visitor type used: " + UsedType);
            System.out.println("[control flow] Unused visitor type: " + NonUsedType);
        }
    }
}
