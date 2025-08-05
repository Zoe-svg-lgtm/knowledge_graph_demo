package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.solver.*;
import com.bjfu.knowledge_graph.config.AiConfig;
import com.bjfu.knowledge_graph.pathfinder.AStarPathfinder;
import com.bjfu.knowledge_graph.pathfinder.CalculationService;
import com.bjfu.knowledge_graph.repository.PhysicalQuantityRepository;
import com.bjfu.knowledge_graph.service.ForceAnalysisService;
import com.bjfu.knowledge_graph.service.SolverService;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SolverServiceImpl implements SolverService {
    @Autowired
    private AiConfig.EnhancedProblemParsingService enhancedProblemParsingService;

    @Autowired
    private ForceAnalysisService forceAnalysisService;

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
            problemParseResult = objectMapper.readValue(enhancedProblemParsingService.parseProblemV1(problem), ProblemParseResult.class);
        }catch (Exception e) {
            log.error("Error parsing problem: {}", e.getMessage());
            return ReturnMsg.fail(ReturnMsg.ERROR, "Problem parsing failed: " + e.getMessage());
        }

        // 2. 检查是否包含力相关的已知量，如果有则进行力分析
        List<KnownVariable> processedKnowns = problemParseResult.getKnowns();
        ForceAnalysisResult forceAnalysis = null;

        if (forceAnalysisService.containsForces(processedKnowns)) {
            log.info("检测到力相关量，开始进行受力分析...");
            forceAnalysis = forceAnalysisService.analyzeForces(processedKnowns);

            // 将各个分力替换为合力
            processedKnowns = replaceIndividualForcesWithResultant(processedKnowns, forceAnalysis);
            log.info("力分析完成: {}", forceAnalysis.getAnalysisDescription());
        }

        // 3. 准备算法输入
        Set<String> startNodeIds =processedKnowns.stream()
                .map(k -> physicalQuantityRepository.findQuantityNodeIdByName(k.getName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String targetNodeId = physicalQuantityRepository.findQuantityNodeIdByName(problemParseResult.getUnknown());

        if (startNodeIds.isEmpty() || targetNodeId == null) {
            return ReturnMsg.fail(ReturnMsg.ERROR, "Known or unknown quantities not found in graph.");
        }

        PathfinderRequest request = new PathfinderRequest(startNodeIds, targetNodeId);

        // 4. 调用算法类执行搜索
        PathfinderResult result = aStarPathfinder.findPath(request);

        // 5. 处理结果
        if (!result.isSuccess()) {
            return ReturnMsg.fail(ReturnMsg.ERROR, "Could not find a solving path.");
        }

        List<SolvingStep> path = result.getPath().orElse(Collections.emptyList());
        log.info("Path found with {} steps.", path.size());

        try {
            // 6. 调用计算服务进行数值计算
            CalculationResult finalResult = calculationService.executeCalculation(path, processedKnowns);

            // 7. 构建成功的响应
            SolverResponse solverResponse = new SolverResponse(finalResult, path, null);
            return ReturnMsg.success(ReturnMsg.SUCCESS, "Solver successful.",solverResponse);

        } catch (Exception e) {
            log.error("Error during calculation: {}", e.getMessage(), e);
            return ReturnMsg.fail(ReturnMsg.ERROR, "Calculation failed: " + e.getMessage());
        }

    }

    private List<KnownVariable> replaceIndividualForcesWithResultant(
            List<KnownVariable> originalKnowns,
            ForceAnalysisResult forceAnalysis) {

        List<KnownVariable> result = new ArrayList<>();

        // 添加非力类型的已知量
        result.addAll(originalKnowns.stream()
                .filter(k -> !"force".equals(k.getType()))
                .collect(Collectors.toList()));

        // 添加合力作为新的已知量（如果合力不为零）
        if (forceAnalysis.getResultantForce() != null &&
                Math.abs(forceAnalysis.getResultantForce().getMagnitude()) > 0.01) {

            KnownVariable resultantForce = new KnownVariable(
                    "合外力",  // 使用标准名称，确保能在知识图谱中找到
                    forceAnalysis.getResultantForce().getMagnitude(),
                    "N",
                    "force",
                    "resultant",
                    forceAnalysis.getResultantForce().getDirection()
            );
            result.add(resultantForce);
            log.info("添加合力到已知量: {}N, 方向: {}",
                    resultantForce.getValue(), resultantForce.getDirection());
        }

        return result;
    }
}
