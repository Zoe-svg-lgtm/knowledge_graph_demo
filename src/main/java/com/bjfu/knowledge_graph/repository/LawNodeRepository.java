package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.LawNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface LawNodeRepository extends Neo4jRepository<LawNode, Long> {
    Optional<LawNode> findByName(String name);
}