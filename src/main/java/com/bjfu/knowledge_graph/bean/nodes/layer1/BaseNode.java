package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;


@Data
public abstract class BaseNode {
    @Id
    @GeneratedValue
    private Long id;

    // name属性在多数节点中都有，放在基类中
    private String name;

}