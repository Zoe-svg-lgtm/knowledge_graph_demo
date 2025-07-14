package com.bjfu.knowledge_graph.bean.solver;

import com.bjfu.knowledge_graph.bean.solver.steps.CalculationStep;
import com.bjfu.knowledge_graph.bean.solver.steps.FindFormulaStep;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // JSON中的'type'字段决定了要实例化哪个子类
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FindFormulaStep.class, name = "find_formula"),
        @JsonSubTypes.Type(value = CalculationStep.class, name = "calculation")
})
@Data
public abstract class SolvingStep {

    private int step;
    private String description;

}