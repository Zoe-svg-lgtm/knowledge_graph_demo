package com.bjfu.knowledge_graph.bean.solver;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProblemParseResult {
    @JsonProperty("knowns")
    private List<KnownVariable> knowns;

    @JsonProperty("unknown")
    private String unknown;
}
