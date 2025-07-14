package com.bjfu.knowledge_graph.bean.solver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KnownVariable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private Double value;

}