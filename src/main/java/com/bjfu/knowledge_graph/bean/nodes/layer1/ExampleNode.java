package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * 代表一个具体的实例、应用场景或特殊的运动形式。
 * 例如，“平抛运动”、“小船过河”、“反冲运动”。
 * 它本身也可以像一个ConceptNode一样，拥有自己的公式、条件等。
 */
@Node("Example")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"formulas", "children"})
public class ExampleNode extends BaseNode {

    /**
     * 对这个例子的简短描述。
     * 例如，对于“平抛运动”，description可以是“v₀沿水平方向, a=g匀变速”。
     */
    @Property("description")
    private String description;

    /**
     * 实例本身也可能有自己的计算公式。
     * 例如: (平抛运动:Example) -[:HAS_FORMULA]-> (x = v₀t:Formula)
     */
    @Relationship(type = "HAS_FORMULA", direction = Relationship.Direction.OUTGOING)
    private Set<Formula> formulas = new HashSet<>();
    
    /**
     * 实例也可能有自己的子节点，用于进一步分解。
     * 例如: (平抛运动:Example) -[:HAS_CHILD]-> (处理方法:Concept)
     */
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private Set<BaseNode> children = new HashSet<>();
}