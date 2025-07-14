package com.bjfu.knowledge_graph.pathfinder;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import com.bjfu.knowledge_graph.repository.FormulaRepository;
import com.bjfu.knowledge_graph.repository.PhysicalQuantityRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 在某个初始化服务中执行
@Component
@Slf4j
public class GraphDistanceInitializer {

    @Autowired
    private FormulaRepository formulaRepository;

    @Autowired
    private PhysicalQuantityRepository quantityRepository;

    private volatile Map<String, Map<String, Integer>> distanceMatrix;


    @PostConstruct
    public void initialize() {
        log.info("Calculating shortest distance matrix for the knowledge graph...");
        // 伪代码，具体实现取决于你的图结构
        this.distanceMatrix = calculateAllPairsShortestPath();
        log.info("Distance matrix calculation complete.");
    }

    /**
     * 计算所有物理量节点对之间的最短路径距离。
     * @return 一个表示距离矩阵的嵌套Map。
     */
    private Map<String, Map<String, Integer>> calculateAllPairsShortestPath() {
        // 1. 获取图中所有的物理量节点
        List<PhysicalQuantity> allQuantities = quantityRepository.findAll();
        if (allQuantities.isEmpty()) {
            log.warn("No physical quantities found in the database. Distance matrix will be empty.");
            return Collections.emptyMap();
        }

        // 2. 构建邻接表，这是图算法的基础
        // Key: 物理量ID, Value: 与之直接相关的邻居物理量ID列表
        Map<String, List<String>> adjacencyList = buildAdjacencyList(allQuantities);

        // 3. 为每个节点运行BFS来计算它到其他所有节点的距离
        Map<String, Map<String, Integer>> allDistances = new ConcurrentHashMap<>();

        for (PhysicalQuantity startNode : allQuantities) {
            Map<String, Integer> distancesFromStartNode = bfs(String.valueOf(startNode.getId()), adjacencyList);
            allDistances.put(String.valueOf(startNode.getId()), distancesFromStartNode);
        }

        return allDistances;
    }

    /**
     * 构建图的邻接表表示。
     * 两个物理量是邻居，如果它们出现在同一个公式中。
     */
    private Map<String, List<String>> buildAdjacencyList(List<PhysicalQuantity> allQuantities) {
        Map<String, List<String>> adjList = new HashMap<>();
        // 初始化所有节点的邻接列表
        for (PhysicalQuantity q : allQuantities) {
            adjList.put(String.valueOf(q.getId()), new ArrayList<>());
        }

        // 遍历所有公式来建立邻接关系
        List<Formula> allFormulas = formulaRepository.findAll();
        for (Formula formula : allFormulas) {
            List<PhysicalQuantity> quantitiesInFormula = new ArrayList<>(formula.getQuantities());
            // 对于公式中的每一对物理量，它们互为邻居
            for (int i = 0; i < quantitiesInFormula.size(); i++) {
                for (int j = i + 1; j < quantitiesInFormula.size(); j++) {
                    PhysicalQuantity q1 = quantitiesInFormula.get(i);
                    PhysicalQuantity q2 = quantitiesInFormula.get(j);
                    adjList.get(String.valueOf(q1.getId())).add(String.valueOf(q2.getId()));
                    adjList.get(String.valueOf(q2.getId())).add(String.valueOf(q1.getId()));
                }
            }
        }
        return adjList;
    }

    /**
     * 从一个起始节点开始执行广度优先搜索（BFS），计算到所有可达节点的距离。
     * @param startNodeId 起始节点的ID
     * @param adjacencyList 图的邻接表
     * @return 从起始节点到其他节点的距离Map
     */
    private Map<String, Integer> bfs(String startNodeId, Map<String, List<String>> adjacencyList) {
        Map<String, Integer> distances = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        // 初始化
        distances.put(startNodeId, 0);
        queue.add(startNodeId);

        while (!queue.isEmpty()) {
            String currentNodeId = queue.poll();
            int currentDistance = distances.get(currentNodeId);

            // 遍历所有邻居
            List<String> neighbors = adjacencyList.getOrDefault(currentNodeId, Collections.emptyList());
            for (String neighborId : neighbors) {
                // 如果邻居还未被访问过
                if (!distances.containsKey(neighborId)) {
                    distances.put(neighborId, currentDistance + 1);
                    queue.add(neighborId);
                }
            }
        }
        return distances;
    }


    public int getDistance(String fromId, String toId) {
        if (distanceMatrix == null || !distanceMatrix.containsKey(fromId)) {
            return Integer.MAX_VALUE; // 或一个很大的数，表示不可达
        }
        return distanceMatrix.get(fromId).getOrDefault(toId, Integer.MAX_VALUE);
    }
}