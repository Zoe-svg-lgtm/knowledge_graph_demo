package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.solver.ForceAnalysisResult;
import com.bjfu.knowledge_graph.bean.solver.ForceComponent;
import com.bjfu.knowledge_graph.bean.solver.KnownVariable;
import com.bjfu.knowledge_graph.service.ForceAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ForceAnalysisServiceImpl implements ForceAnalysisService {
    
    @Override
    public boolean containsForces(List<KnownVariable> knownQuantities) {
        return knownQuantities.stream()
                .anyMatch(q -> "force".equals(q.getType()));
    }
    
    @Override
    public ForceAnalysisResult analyzeForces(List<KnownVariable> knownQuantities) {
        log.info("开始分析力的合成...");
        
        // 提取所有力相关的量
        List<KnownVariable> forces = knownQuantities.stream()
                .filter(q -> "force".equals(q.getType()))
                .collect(Collectors.toList());
        
        if (forces.isEmpty()) {
            log.warn("没有找到力相关的已知量");
            return new ForceAnalysisResult();
        }
        
        List<ForceComponent> forceComponents = new ArrayList<>();
        double totalX = 0.0;
        double totalY = 0.0;
        
        for (KnownVariable force : forces) {
            ForceComponent component = createForceComponent(force, knownQuantities);
            forceComponents.add(component);
            
            totalX += component.getX();
            totalY += component.getY();
            
            log.info("处理力: {} = {}N, 方向: {}, 分量: ({}, {})", 
                    component.getName(), component.getMagnitude(), 
                    component.getDirection(), component.getX(), component.getY());
        }
        
        // 计算合力
        double resultantMagnitude = Math.sqrt(totalX * totalX + totalY * totalY);
        double resultantAngle = Math.atan2(totalY, totalX);
        String resultantDirection = getDirectionDescription(resultantAngle);
        
        ForceComponent resultantForce = new ForceComponent(
                "合力", resultantMagnitude, resultantDirection, resultantAngle,
                totalX, totalY, "N"
        );
        
        boolean isEquilibrium = Math.abs(resultantMagnitude) < 0.01; // 考虑数值误差
        
        String description = generateAnalysisDescription(forceComponents, resultantForce, isEquilibrium);
        
        log.info("力分析完成 - 合力大小: {}N, 方向: {}, 平衡状态: {}", 
                resultantMagnitude, resultantDirection, isEquilibrium);
        
        return new ForceAnalysisResult(forceComponents, resultantForce, isEquilibrium, description);
    }
    /**
     * 创建一个力分量对象
     */
    private ForceComponent createForceComponent(KnownVariable force, List<KnownVariable> allQuantities) {
        String name = force.getName();
        double magnitude = force.getValue();
        String direction = force.getDirection() != null ? force.getDirection() : "未指定";
        double angle = parseDirection(force.getDirection(), force.getSubType(), allQuantities);
        
        double x = magnitude * Math.cos(angle);
        double y = magnitude * Math.sin(angle);
        
        return new ForceComponent(name, magnitude, direction, angle, x, y, "N");
    }

    /**
     * 解析力方向描述
     */
    private double parseDirection(String direction, String forceType, List<KnownVariable> allQuantities) {
        if (direction == null) {
            // 根据力的类型推断默认方向
            return getDefaultAngleByForceType(forceType);
        }
        
        // 解析方向描述
        direction = direction.toLowerCase().trim();
        
        if (direction.contains("向下") || direction.contains("竖直向下") || forceType.equals("gravity")) {
            return -Math.PI / 2; // -90度
        } else if (direction.contains("向上") || direction.contains("竖直向上")) {
            return Math.PI / 2; // 90度
        } else if (direction.contains("向右") || direction.contains("水平向右")) {
            return 0; // 0度
        } else if (direction.contains("向左") || direction.contains("水平向左")) {
            return Math.PI; // 180度
        } else if (direction.contains("度")) {
            // 尝试解析角度
            try {
                String angleStr = direction.replaceAll("[^0-9.-]", "");
                return Math.toRadians(Double.parseDouble(angleStr));
            } catch (NumberFormatException e) {
                log.warn("无法解析角度: {}", direction);
            }
        }
        
        return getDefaultAngleByForceType(forceType);
    }

    /**
     * 根据力类型获取默认方向
     */
    private double getDefaultAngleByForceType(String forceType) {
        if (forceType == null) return 0;
        
        switch (forceType.toLowerCase()) {
            case "gravity":
            case "weight":
                return -Math.PI / 2; // 重力向下
            case "normal":
                return Math.PI / 2;  // 法向力向上
            case "friction":
                return Math.PI;      // 摩擦力默认向左（与运动方向相反）
            case "tension":
                return 0;            // 拉力默认向右
            case "applied":
                return 0;            // 外加力默认向右
            default:
                return 0;
        }
    }
    
    private String getDirectionDescription(double angle) {
        double degrees = Math.toDegrees(angle);
        
        if (Math.abs(degrees) < 5) {
            return "水平向右";
        } else if (Math.abs(degrees - 90) < 5) {
            return "竖直向上";
        } else if (Math.abs(degrees + 90) < 5) {
            return "竖直向下";
        } else if (Math.abs(Math.abs(degrees) - 180) < 5) {
            return "水平向左";
        } else if (degrees > 0 && degrees < 90) {
            return String.format("与水平方向成%.1f°向上", degrees);
        } else if (degrees > 90 && degrees < 180) {
            return String.format("与水平方向成%.1f°向上偏左", 180 - degrees);
        } else if (degrees < 0 && degrees > -90) {
            return String.format("与水平方向成%.1f°向下", -degrees);
        } else {
            return String.format("与水平方向成%.1f°向下偏左", degrees + 180);
        }
    }
    
    private String generateAnalysisDescription(List<ForceComponent> forces, 
                                             ForceComponent resultant, 
                                             boolean isEquilibrium) {
        StringBuilder sb = new StringBuilder();
        sb.append("受力分析：\n");
        
        for (ForceComponent force : forces) {
            sb.append(String.format("- %s: %.2fN, %s\n", 
                    force.getName(), force.getMagnitude(), force.getDirection()));
        }
        
        sb.append(String.format("\n合力分析：\n"));
        sb.append(String.format("- x方向合力: %.2fN\n", resultant.getX()));
        sb.append(String.format("- y方向合力: %.2fN\n", resultant.getY()));
        sb.append(String.format("- 合力大小: %.2fN\n", resultant.getMagnitude()));
        sb.append(String.format("- 合力方向: %s\n", resultant.getDirection()));
        
        if (isEquilibrium) {
            sb.append("\n物体处于平衡状态（合力为零）");
        } else {
            sb.append(String.format("\n物体不平衡，合力为%.2fN", resultant.getMagnitude()));
        }
        
        return sb.toString();
    }
}