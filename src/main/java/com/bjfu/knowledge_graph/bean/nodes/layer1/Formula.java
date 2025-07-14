package com.bjfu.knowledge_graph.bean.nodes.layer1;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Formula")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Formula extends BaseNode { // name是唯一索引
    @Property("expression")
    private String expression;

    // 关系定义
    @JsonManagedReference
    @Relationship(type = "CONTAINS_QUANTITY_RELATIONSHIP", direction = Relationship.Direction.OUTGOING)
    private List<PhysicalQuantity> quantities;

    @JsonManagedReference
    @Relationship(type = "CONTAINS_CONSTANT_RELATIONSHIP", direction = Relationship.Direction.OUTGOING)
    private List<Constant> constants;
}