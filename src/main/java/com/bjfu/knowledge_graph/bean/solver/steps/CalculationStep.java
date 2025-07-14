package com.bjfu.knowledge_graph.bean.solver.steps;

import com.bjfu.knowledge_graph.bean.solver.CalculationResult;
import com.bjfu.knowledge_graph.bean.solver.SolvingStep;
import lombok.Data;

@Data
public class CalculationStep extends SolvingStep {
    
    private String formulaNodeId;
    private CalculationResult intermediateResult;

    
    public CalculationStep(int step, String description, String formulaNodeId, CalculationResult intermediateResult) {
        setStep(step);
        setDescription(description);
        this.formulaNodeId = formulaNodeId;
        this.intermediateResult = intermediateResult;
    }
}