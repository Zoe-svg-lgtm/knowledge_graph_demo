package com.bjfu.knowledge_graph.bean.vo;
import lombok.Data;

@Data
public class PhysicalQuantityVO {
    private String symbol;
    private String unit;
    private boolean isVector;
    private String description;
}
