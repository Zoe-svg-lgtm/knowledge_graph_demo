package com.bjfu.knowledge_graph.bean.relationships;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Constant;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
@Data
@EqualsAndHashCode(callSuper = true)
public class ContainsConstantRelationship extends BaseRelationship<Formula, Constant>{

    @Override
    public void setStartNode(Formula startNode) {
        super.setStartNode(startNode);
    }

    @Override
    public void setEndNode(Constant endNode) {
        super.setEndNode(endNode);
    }
}
