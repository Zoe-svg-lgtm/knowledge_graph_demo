package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表一个核心的物理学概念，如“重力”、“动能”、“曲线运动”。
 * 它是知识网络中的核心枢纽节点。
 */
@Node("Concept")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"children", "formulas", "conditions", "characteristics", "examples", "subConcepts"})
// 在 EqualsAndHashCode 中排除集合类型字段，防止因循环引用导致栈溢出，并提高性能。
public class ConceptNode extends BaseNode {
    private String text;

    /**
     * 概念的详细定义或描述。
     * 例如，对于“力”，它的definition是“力是物体间的相互作用”。
     */
    @Property("definition")
    private String definition;

    // --- 关系定义 ---

    /**
     * 用于构建思维导图的父子层级结构。
     * 这是一个非常通用的关系，可以连接到任何类型的子节点（CategoryNode, ConceptNode, LawNode等）。
     * 例如: (力:Concept) -[:HAS_CHILD]-> (常见的三种力:Category)
     */
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private Set<BaseNode> children = new HashSet<>();

    /**
     * 一个概念可以包含多个子概念，形成更具体的层级。
     * 例如: (摩擦力:Concept) -[:HAS_SUBCONCEPT]-> (滑动摩擦力:Concept)
     * 注意：这个关系比HAS_CHILD更具语义化，可以和HAS_CHILD并存或择一使用。
     * 为了简化，可以只用HAS_CHILD，但这个更精确。
     */
    @Relationship(type = "HAS_SUBCONCEPT", direction = Relationship.Direction.OUTGOING)
    private Set<ConceptNode> subConcepts = new HashSet<>();

    /**
     * 连接到描述该概念的公式。
     * 例如: (重力:Concept) -[:HAS_FORMULA]-> (G=mg:Formula)
     * 这里连接的是你已经定义好的、复杂的Formula节点。
     */
    @Relationship(type = "HAS_FORMULA", direction = Relationship.Direction.OUTGOING)
    private Set<Formula> formulas = new HashSet<>();

    /**
     * 连接到该概念成立或存在的条件。
     * 例如: (弹力:Concept) -[:HAS_CONDITION]-> (产生条件:ConditionNode)
     */
    @Relationship(type = "HAS_CONDITION", direction = Relationship.Direction.OUTGOING)
    private Set<ConditionNode> conditions = new HashSet<>();

    /**
     * 连接到描述该概念的特点或性质。
     * 例如: (力:Concept) -[:HAS_CHARACTERISTIC]-> (力的三要素:CharacteristicNode)
     */
    @Relationship(type = "HAS_CHARACTERISTIC", direction = Relationship.Direction.OUTGOING)
    private Set<CharacteristicNode> characteristics = new HashSet<>();

    /**
     * 连接到用以说明该概念的具体实例或应用。
     * 例如: (曲线运动:Concept) -[:HAS_EXAMPLE]-> (平抛运动:ExampleNode)
     */
    @Relationship(type = "HAS_EXAMPLE", direction = Relationship.Direction.OUTGOING)
    private Set<ExampleNode> examples = new HashSet<>();

    /**
     * (可选，但推荐) 连接到一个更高层次的分类。
     * 例如: (重力:Concept) -[:BELONGS_TO]-> (常见的三种力:CategoryNode)
     * 这条关系是“自下而上”的，可以帮助进行归纳查询。
     * 如果主要用HAS_CHILD进行自上而下的构建，这条可以省略以简化模型。
     */
    // @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    // private CategoryNode category;

}