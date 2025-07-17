package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Theorem")
@Data
@EqualsAndHashCode(callSuper = true)
public class TheoremNode extends BaseNode {
    @Property("content")
    private String content;
}