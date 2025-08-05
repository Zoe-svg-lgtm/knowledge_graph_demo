package com.bjfu.knowledge_graph.bean.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathfinderResult {
    private boolean success;
    private List<SolvingStep> path;

    
    public static PathfinderResult success(List<SolvingStep> path) {
        return new PathfinderResult(true, path);
    }

    public static PathfinderResult failure() {
        return new PathfinderResult(false, null);
    }
    
    public boolean isSuccess() { return success; }

    // 使用Optional来优雅地处理可能不存在的路径
    public Optional<List<SolvingStep>> getPath() {
        return Optional.ofNullable(path);
    }
}