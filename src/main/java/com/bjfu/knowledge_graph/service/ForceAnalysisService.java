package com.bjfu.knowledge_graph.service;

import com.bjfu.knowledge_graph.bean.solver.ForceAnalysisResult;
import com.bjfu.knowledge_graph.bean.solver.KnownVariable;

import java.util.List;

public interface ForceAnalysisService {
    ForceAnalysisResult analyzeForces(List<KnownVariable> knownQuantities);
    boolean containsForces(List<KnownVariable> knownQuantities);
}