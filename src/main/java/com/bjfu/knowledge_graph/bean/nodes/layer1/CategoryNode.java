package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表一个用于组织和分类的节点，它本身不是一个具体的物理概念。
 * 类似于思维导图中的分组框。
 * 例如：“基本概念”、“基本运动形式”、“规律”、“特例”、“常见的三种力”。
 */
@Node("Category")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"children"})
public class CategoryNode extends BaseNode {

    /**
     * CategoryNode的核心功能是通过这个关系来包含一组子节点。
     * 这些子节点可以是任何类型（ConceptNode, LawNode, 甚至另一个CategoryNode）。
     * 例如： (常见的三种力:Category) -[:HAS_CHILD]-> (重力:Concept)
     *        (常见的三种力:Category) -[:HAS_CHILD]-> (弹力:Concept)
     *        (常见的三种力:Category) -[:HAS_CHILD]-> (摩擦力:Concept)
     */
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private Set<BaseNode> children = new HashSet<>();

}