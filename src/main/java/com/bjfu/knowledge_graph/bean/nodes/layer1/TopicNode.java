package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Topic")
@Data
@EqualsAndHashCode(callSuper = true)
public class TopicNode extends BaseNode {
    // 直接注解，无需创建关系实体类
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    private Set<BaseNode> children = new HashSet<>();
}