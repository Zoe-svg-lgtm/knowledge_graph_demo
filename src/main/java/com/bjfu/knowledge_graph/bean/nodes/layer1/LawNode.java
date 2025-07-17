package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表一个物理学的定律、定理或原理。
 * 这是知识图谱中非常重要的一类核心节点。
 * 例如：“牛顿第一定律”、“动能定理”、“动量守恒定律”。
 */
@Node("Law")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"formulas", "conditions", "examples", "children"})
public class LawNode extends BaseNode {

    /**
     * 定律或定理的完整内容描述。
     * 例如，对于牛顿第一定律，其content是“一切物体总保持匀速直线运动状态或静止状态，直到有外力迫使它改变这种状态为止。”
     */
    @Property("content")
    private String content;

    // --- 关系定义 ---

    /**
     * 定律通常有其数学表达式，即公式。
     * 例如: (牛顿第二定律:Law) -[:HAS_FORMULA]-> (F=ma:Formula)
     */
    @Relationship(type = "HAS_FORMULA", direction = Relationship.Direction.OUTGOING)
    private Set<Formula> formulas = new HashSet<>();

    /**
     * 任何定律都有其成立的条件或适用范围。
     * 例如: (动量守恒定律:Law) -[:HAS_CONDITION]-> (系统不受外力或合外力为零:ConditionNode)
     */
    @Relationship(type = "HAS_CONDITION", direction = Relationship.Direction.OUTGOING)
    private Set<ConditionNode> conditions = new HashSet<>();

    /**
     * 定律的应用实例。
     * 例如: (动量守恒定律:Law) -[:HAS_EXAMPLE]-> (反冲运动:ExampleNode)
     */
    @Relationship(type = "HAS_EXAMPLE", direction = Relationship.Direction.OUTGOING)
    private Set<ExampleNode> examples = new HashSet<>();
    
    /**
     * 定律也可能有自己的子节点，用于解释其推论或关键点。
     * 例如: (牛顿第三定律:Law) -[:HAS_CHILD]-> (作用力与反作用力特点:CharacteristicNode)
     */
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private Set<BaseNode> children = new HashSet<>();
}