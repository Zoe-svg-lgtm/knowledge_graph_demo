package com.bjfu.knowledge_graph.service;

import com.bjfu.knowledge_graph.bean.solver.SolverResponse;
import com.bjfu.knowledge_graph.utils.ReturnMsg;

public interface SolverService {
    ReturnMsg<SolverResponse> solveProblem(String problem);

}
