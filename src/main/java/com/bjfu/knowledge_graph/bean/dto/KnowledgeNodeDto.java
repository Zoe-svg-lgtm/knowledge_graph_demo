package com.bjfu.knowledge_graph.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class KnowledgeNodeDto {
    @JsonProperty("label")
    private String label;
    @JsonProperty("name")
    private String name;
    @JsonProperty("properties")
    private Map<String, String> properties;
    @JsonProperty("children")
    private List<KnowledgeNodeDto> children;
}