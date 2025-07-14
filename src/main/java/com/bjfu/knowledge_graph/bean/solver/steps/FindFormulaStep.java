package com.bjfu.knowledge_graph.bean.solver.steps;

import com.bjfu.knowledge_graph.bean.solver.SolvingStep;
import lombok.Data;

import java.util.List;

@Data
public class FindFormulaStep extends SolvingStep {
    
    private String formulaNodeId;
    private List<String> inputNodeIds;
    private String outputNodeId;
    
    public FindFormulaStep(int step, String description, String formulaNodeId, List<String> inputNodeIds, String outputNodeId) {
        setStep(step);
        setDescription(description);
        this.formulaNodeId = formulaNodeId;
        this.inputNodeIds = inputNodeIds;
        this.outputNodeId = outputNodeId;
    }

}