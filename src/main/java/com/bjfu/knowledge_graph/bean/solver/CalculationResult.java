package com.bjfu.knowledge_graph.bean.solver;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalculationResult {
    private String name;
    private Double value;
    private String unit;
}
