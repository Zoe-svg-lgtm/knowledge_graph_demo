package com.bjfu.knowledge_graph.bean.solver;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForceComponent {
    private String name;           // 力的名称（重力、摩擦力等）
    private double magnitude;      // 力的大小
    private String direction;      // 力的方向描述
    private double angle;          // 力与x轴的夹角（弧度）
    private double x;             // x方向分量
    private double y;             // y方向分量
    private String unit;          // 单位
}