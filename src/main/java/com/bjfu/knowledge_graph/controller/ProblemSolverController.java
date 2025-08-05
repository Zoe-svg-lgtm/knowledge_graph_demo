package com.bjfu.knowledge_graph.controller;

import com.bjfu.knowledge_graph.bean.solver.SolverResponse;
import com.bjfu.knowledge_graph.service.SolverService;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProblemSolverController {
    @Autowired
    private SolverService solverService;

     @PostMapping("/solve")
     public ReturnMsg<SolverResponse> solveProblem(@RequestParam("problem") String problem) {
         return solverService.solveProblem(problem);
     }
}
