package com.bjfu.knowledge_graph.bean.solver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SolverResponse {
    private CalculationResult result;
    private List<SolvingStep> solvingPath;
    private String message;
}
