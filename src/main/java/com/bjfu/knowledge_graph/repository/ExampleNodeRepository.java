package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.ExampleNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface ExampleNodeRepository extends Neo4jRepository<ExampleNode, Long> {
    /**
     * 根据名称查找示例节点
     * @param name 示例名称
     * @return 示例节点
     */
    Optional<ExampleNode> findByName(String name);
}
