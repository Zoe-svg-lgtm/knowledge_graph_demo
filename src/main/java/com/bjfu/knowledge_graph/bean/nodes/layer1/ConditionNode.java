package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * 代表一个条件、前提、适用范围或注意事项。
 * 例如，“牛顿定律的适用范围”、“弹力的产生条件”。
 */
@Node("Condition")
@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionNode extends BaseNode {

    /**
     * 条件的具体文本内容。
     * 例如：“①物体间直接接触 ②接触面发生弹性形变”
     * 或 “宏观、低速、惯性参考系”。
     */
    @Property("text")
    private String text;
}