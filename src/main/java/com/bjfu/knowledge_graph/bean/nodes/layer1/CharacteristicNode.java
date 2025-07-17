package com.bjfu.knowledge_graph.bean.nodes.layer1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

/**
 * 代表一个概念或事物的特点、性质、关键点或构成要素。
 * 例如，“力的三要素”、“牛顿第二定律的矢量性”。
 */
@Node("Characteristic")
@Data
@EqualsAndHashCode(callSuper = true)
public class CharacteristicNode extends BaseNode {

    /**
     * 特征或描述的具体文本内容。
     * 例如：“大小、方向、作用点”
     * 或 “F与F'大小相等方向相反、同性质、作用时间相同”。
     */
    @Property("text")
    private String text;
}