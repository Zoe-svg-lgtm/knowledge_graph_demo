package com.bjfu.knowledge_graph.bean.relationships;

import com.bjfu.knowledge_graph.bean.nodes.layer1.BaseNode;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

/**
 * 所有关系实体的抽象基类。
 *
 * @param <S> 起始节点的类型 (Start Node)
 * @param <E> 结束节点的类型 (End Node)
 */
@RelationshipProperties
@Data
@NoArgsConstructor
public abstract class BaseRelationship<S extends BaseNode, E extends BaseNode> {

    @RelationshipId
    @GeneratedValue
    private Long id; // SDN 内部的关系ID

    @TargetNode
    private S startNode;

    @TargetNode
    private E endNode;

    public BaseRelationship(S startNode, E endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
    }

}