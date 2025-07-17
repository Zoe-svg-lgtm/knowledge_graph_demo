package com.bjfu.knowledge_graph.bean.relationships;

import com.bjfu.knowledge_graph.bean.nodes.layer1.CharacteristicNode;
import com.bjfu.knowledge_graph.bean.nodes.layer1.ConceptNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
@Data
@EqualsAndHashCode(callSuper = true)
public class HasCharacteristic extends BaseRelationship<ConceptNode, CharacteristicNode> {

}
