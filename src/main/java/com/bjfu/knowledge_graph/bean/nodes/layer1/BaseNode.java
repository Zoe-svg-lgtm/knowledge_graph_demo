package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;


@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseNode {
    @Id
    @GeneratedValue
    private Long id;

    // name属性在多数节点中都有，放在基类中
    private String name;

}