package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.solver.*;
import com.bjfu.knowledge_graph.config.AiConfig;
import com.bjfu.knowledge_graph.pathfinder.AStarPathfinder;
import com.bjfu.knowledge_graph.pathfinder.CalculationService;
import com.bjfu.knowledge_graph.repository.PhysicalQuantityRepository;
import com.bjfu.knowledge_graph.service.SolverService;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SolverServiceImpl implements SolverService {
    @Autowired
    private AiConfig.ProblemParsingService problemParsingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AStarPathfinder aStarPathfinder;

    @Autowired
    private PhysicalQuantityRepository physicalQuantityRepository;

    @Autowired
    private CalculationService calculationService;

    @Override
    public ReturnMsg<SolverResponse> solveProblem(String problem) {
        if(problem == null || problem.isEmpty()) {
            return ReturnMsg.fail(ReturnMsg.ERROR,"Problem cannot be null or empty");
        }
        ProblemParseResult problemParseResult;
        //封装解析结果
        log.info("Received problem: {}", problem);
        try{
            problemParseResult = objectMapper.readValue(problemParsingService.parseProblem(problem), ProblemParseResult.class);
        }catch (Exception e) {
            log.error("Error parsing problem: {}", e.getMessage());
            return ReturnMsg.fail(ReturnMsg.ERROR, "Problem parsing failed: " + e.getMessage());
        }

        // 2. 准备算法输入
        Set<String> startNodeIds = problemParseResult.getKnowns().stream()
                .map(k -> physicalQuantityRepository.findQuantityNodeIdByName(k.getName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String targetNodeId = physicalQuantityRepository.findQuantityNodeIdByName(problemParseResult.getUnknown());

        if (startNodeIds.isEmpty() || targetNodeId == null) {
            return ReturnMsg.fail(ReturnMsg.ERROR, "Known or unknown quantities not found in graph.");
        }

        PathfinderRequest request = new PathfinderRequest(startNodeIds, targetNodeId);

        // 3. 调用算法类执行搜索
        log.info("Starting pathfinding...");
        PathfinderResult result = aStarPathfinder.findPath(request);

        // 4. 处理结果
        if (!result.isSuccess()) {
            return ReturnMsg.fail(ReturnMsg.ERROR, "Could not find a solving path.");
        }

        List<SolvingStep> path = result.getPath().orElse(Collections.emptyList());
        log.info("Path found with {} steps.", path.size());

        try {
            // 5. 调用计算服务进行数值计算
            CalculationResult finalResult = calculationService.executeCalculation(path, problemParseResult.getKnowns());

            // 6. 构建成功的响应
            SolverResponse solverResponse = new SolverResponse(finalResult, path, null);
            return ReturnMsg.success(ReturnMsg.SUCCESS, "Solver successful.",solverResponse);

        } catch (Exception e) {
            log.error("Error during calculation: {}", e.getMessage(), e);
            return ReturnMsg.fail(ReturnMsg.ERROR, "Calculation failed: " + e.getMessage());
        }

    }
}
