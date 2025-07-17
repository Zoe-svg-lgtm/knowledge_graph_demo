package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.CharacteristicNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface CharacteristicNodeRepository extends Neo4jRepository<CharacteristicNode, Long> {
    /**
     * 根据名称查找特征节点
     * @param name 特征名称
     * @return 特征节点
     */
    Optional<CharacteristicNode> findByName(String name);
}
