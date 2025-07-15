package com.bjfu.knowledge_graph.bean.nodes.layer2;

import com.bjfu.knowledge_graph.bean.nodes.layer1.BaseNode;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.List;

/**
 * 抽象场景基类 (应用层)
 * 定义了所有物理场景的通用属性和关系。
 * 在Neo4j中，所有继承它的子类节点都会自动拥有 "Scenario" 这个标签。
 */
@Node("Scenario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Scenario extends BaseNode {


    @Property("scenarioName")
    private String scenarioName;

    @Property("description")
    private String description;
    // 哪些关键词说明这个场景
    @Property("keywords")
    private List<String> keywords;
    // 这个场景的难度级别
    @Property("difficulty")
    private int difficulty;

    // 核心公式
    @Relationship(type = "CORE_FORMULA", direction = Relationship.Direction.OUTGOING)
    private List<Formula> coreFormulas;

    // 这个用于复杂的场景层次结构
    @Relationship(type = "IS_A_TYPE_OF", direction = Relationship.Direction.OUTGOING)
    private Scenario parentScenario;

    @Relationship(type = "IS_A_TYPE_OF", direction = Relationship.Direction.INCOMING)
    private List<Scenario> subScenarios;
}