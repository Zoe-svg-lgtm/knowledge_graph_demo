package com.bjfu.knowledge_graph.pathfinder;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import com.bjfu.knowledge_graph.bean.solver.PathfinderRequest;
import com.bjfu.knowledge_graph.bean.solver.PathfinderResult;
import com.bjfu.knowledge_graph.bean.solver.SolvingStep;
import com.bjfu.knowledge_graph.bean.solver.steps.FindFormulaStep;
import com.bjfu.knowledge_graph.repository.FormulaRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AStarPathfinder {
    @Autowired
    private FormulaRepository formulaRepository;

    @Autowired
    private GraphDistanceInitializer distanceService;


    /**
     * 主执行方法，寻找从起点到目标的求解路径
     * @param request 包含起点和终点信息的请求
     * @return 包含路径或失败状态的结果
     */
    public PathfinderResult findPath(PathfinderRequest request) {
        PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
        Map<String, Double> gScores = new HashMap<>();
        Map<String, Derivation> derivationPath = new HashMap<>();

        // 初始化
        for (String startId : request.getStartNodeIds()) {
            gScores.put(startId, 0.0);
            double hScore = calculateHeuristic(startId, request.getTargetNodeId());
            openSet.add(new AStarNode(startId, 0.0, hScore));
        }

        // A* 循环
        while (!openSet.isEmpty()) {
            AStarNode currentNode = openSet.poll();
            String currentId = currentNode.quantityId;

            if (currentId.equals(request.getTargetNodeId())) {
                List<SolvingStep> path = reconstructPath(derivationPath, request.getTargetNodeId());
                return PathfinderResult.success(path);
            }

            // 找到所有与当前“新解锁”的物理量相关的公式
            List<Formula> relatedFormulas = formulaRepository.findFormulasContainingQuantity(Long.parseLong(currentId));

            for (Formula formula : relatedFormulas) {
                List<PhysicalQuantity> allQuantitiesInFormula = formula.getQuantities();

                // 检查这个公式现在是否变得“可解”
                List<PhysicalQuantity> unknowns = allQuantitiesInFormula.stream()
                        .filter(q -> !gScores.containsKey(String.valueOf(q.getId())))
                        .collect(Collectors.toList());

                if (unknowns.size() == 1) {
                    // 如果可解，这个唯一的未知量就是我们新发现的“邻居”
                    PhysicalQuantity newSolvableQuantity = unknowns.get(0);
                    String neighborId = String.valueOf(newSolvableQuantity.getId());

                    // 1. 找到解锁这个公式需要的所有输入物理量
                    List<String> inputIds = allQuantitiesInFormula.stream()
                            .map(q -> String.valueOf(q.getId()))
                            .filter(id -> !id.equals(neighborId)) // 排除掉我们要求解的那个
                            .collect(Collectors.toList());

                    // 2. 在这些输入中，找到gScore最大的那个（也就是最晚被解锁的那个）
                    double maxInputGScore = 0.0;
                    for (String inputId : inputIds) {
                        maxInputGScore = Math.max(maxInputGScore, gScores.get(inputId));
                    }

                    // 3. 新节点的gScore = 最晚解锁的输入的gScore + 1步（使用本公式）
                    double tentativeGScore = maxInputGScore + 1;


                    // 检查这是否是一条更优的路径
                    if (tentativeGScore < gScores.getOrDefault(neighborId, Double.POSITIVE_INFINITY)) {
                        // 如果是，更新路径信息
                        derivationPath.put(neighborId, new Derivation(String.valueOf(formula.getId()), inputIds));
                        gScores.put(neighborId, tentativeGScore);
                        double hScore = calculateHeuristic(neighborId, request.getTargetNodeId());
                        openSet.add(new AStarNode(neighborId, tentativeGScore, hScore));
                    }
                }
            }
        }
        return PathfinderResult.failure();
    }

    // 启发函数
    private double calculateHeuristic(String currentId, String targetId) {
        // 优先级1: 检查是否能通过某个公式直接求解目标
        List<Formula> formulas = formulaRepository.findFormulasContainingQuantity(Long.parseLong(targetId));
        for (Formula formula : formulas) {
            List<PhysicalQuantity> quantities = formula.getQuantities();
            // 如果这个公式同时包含了当前节点和目标节点
            if (quantities.stream().anyMatch(q -> q.getId().equals(targetId))) {
                // 这是最佳情况，给出最低的启发值
                return 1.0;
            }
        }

        // 优先级2: 如果不能直接求解，则使用预计算的距离作为启发值
        // 这个距离代表了从当前节点到目标节点“理论上”最少还需要几步
        int distance = distanceService.getDistance(currentId, targetId);

        // 如果不可达，给一个非常大的值
        if (distance == Integer.MAX_VALUE) {
            return Double.POSITIVE_INFINITY;
        }
        // 返回预计算的距离。因为这个距离是理论最短路，所以它符合A*启发函数"不得高于实际代价"的要求。
        return (double) distance;
    }

    // 路径回溯
    private List<SolvingStep> reconstructPath(Map<String, Derivation> derivationPath, String targetId) {
        LinkedList<SolvingStep> path = new LinkedList<>();
        String currentTarget = targetId;
        while (derivationPath.containsKey(currentTarget)) {
            Derivation d = derivationPath.get(currentTarget);
            // 这里可以填充更详细的描述信息
            String description = String.format("Use formula %s to find %s", d.getFormulaId(), currentTarget);
            FindFormulaStep step = new FindFormulaStep(0, description, d.getFormulaId(), d.getInputQuantityIds(), currentTarget);
            path.addFirst(step);
            
            // 简单的回溯，找到第一个可推导的父节点
            currentTarget = d.getInputQuantityIds().stream()
                .filter(derivationPath::containsKey)
                .findFirst()
                .orElse(null);
                
            if (currentTarget == null) break;
        }

        // 重新编号
        for (int i = 0; i < path.size(); i++) {
            path.get(i).setStep(i + 1);
        }
        return path;
    }

    // --- 内部辅助类 ---
    private static class AStarNode implements Comparable<AStarNode> {
        String quantityId;
        double gScore, hScore, fScore;

        AStarNode(String quantityId, double gScore, double hScore) {
            this.quantityId = quantityId;
            this.gScore = gScore;
            this.hScore = hScore;
            this.fScore = gScore + hScore;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    private static class Derivation {
        private final String formulaId;
        private final List<String> inputQuantityIds;

        public Derivation(String formulaId, List<String> inputQuantityIds) {
            this.formulaId = formulaId;
            this.inputQuantityIds = inputQuantityIds;
        }
        public String getFormulaId() { return formulaId; }
        public List<String> getInputQuantityIds() { return inputQuantityIds; }
    }
}