package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("PhysicalQuantity")
@Data
@EqualsAndHashCode(callSuper = true)
public class PhysicalQuantity extends BaseNode {
    @Property("symbol")
    private String symbol; // 唯一索引将在配置中创建

    @Property("unit")
    private String unit;

    @Property("is_vector")
    private boolean isVector;

    @Property("description")
    private String description;
}