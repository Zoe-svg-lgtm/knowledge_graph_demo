package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer2.Scenario;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SceneRepository extends Neo4jRepository<Scenario, Long> {

    /**
     * 根据场景名称查询场景是否存在
     *
     * @param name 场景名称
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByScenarioName(String name);


}
