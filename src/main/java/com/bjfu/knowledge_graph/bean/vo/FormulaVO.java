package com.bjfu.knowledge_graph.bean.vo;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Constant;
import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import lombok.Data;

import java.util.List;
@Data
public class FormulaVO {
    private Long id;
    private String formulaName;
    private String expression;
    private List<PhysicalQuantity> quantities;
    private List<Constant> constants;
}
