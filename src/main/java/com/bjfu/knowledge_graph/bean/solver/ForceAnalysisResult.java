package com.bjfu.knowledge_graph.bean.solver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForceAnalysisResult {
    private List<ForceComponent> individualForces;  // 各个分力
    private ForceComponent resultantForce;          // 合力
    private boolean isEquilibrium;                  // 是否平衡
    private String analysisDescription;             // 分析描述
}