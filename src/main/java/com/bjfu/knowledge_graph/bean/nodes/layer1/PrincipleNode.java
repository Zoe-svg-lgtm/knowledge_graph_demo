package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Principle")
@Data
@EqualsAndHashCode(callSuper = true)
public class PrincipleNode extends BaseNode {
    @Property("text") // 或 content
    private String text;
}