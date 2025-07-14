package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("Constant")
@Data
@EqualsAndHashCode(callSuper = true)
public class Constant extends BaseNode {
    @Property("symbol")
    private String symbol; // 唯一索引

    @Property("value")
    private Double value;

    @Property("unit")
    private String unit;
}