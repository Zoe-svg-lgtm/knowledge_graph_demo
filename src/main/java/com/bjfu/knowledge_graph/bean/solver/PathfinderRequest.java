package com.bjfu.knowledge_graph.bean.solver;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class PathfinderRequest {
    private Set<String> startNodeIds; // 所有已知量的ID集合
    private String targetNodeId;     // 目标量的ID

}