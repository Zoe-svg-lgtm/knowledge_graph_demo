package com.bjfu.knowledge_graph.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class SceneDTO {
    private String name;
    private String description;
    private List<String> keywords;
    private int difficulty;
    private List<String> coreFormulas;
}
