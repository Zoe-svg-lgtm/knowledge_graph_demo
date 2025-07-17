package com.bjfu.knowledge_graph.bean.relationships;

import com.bjfu.knowledge_graph.bean.nodes.layer1.ApplicationNode;
import com.bjfu.knowledge_graph.bean.nodes.layer1.BaseNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
@Data
@EqualsAndHashCode(callSuper = true)
public class HasApplication extends BaseRelationship<BaseNode, ApplicationNode> {}