package com.bjfu.knowledge_graph.bean.solver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnownVariable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private Double value;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("type")
    private String type;

    @JsonProperty("subType")
    private String subType;

    @JsonProperty("direction")
    private String direction;

}