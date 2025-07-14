package com.bjfu.knowledge_graph.bean.relationships;


import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
@Data
@EqualsAndHashCode(callSuper = true)
public class ContainsQuantityRelationship extends BaseRelationship<Formula, PhysicalQuantity> {
    // This relationship has no additional properties.
    @Override
    public void setStartNode(Formula startNode) {
        super.setStartNode(startNode);
    }

    @Override
    public void setEndNode(PhysicalQuantity endNode) {
        super.setEndNode(endNode);
    }
}