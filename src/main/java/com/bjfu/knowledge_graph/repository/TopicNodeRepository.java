package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.TopicNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface TopicNodeRepository extends Neo4jRepository<TopicNode, Long> {

    Optional<TopicNode> findByName(String name);
}
