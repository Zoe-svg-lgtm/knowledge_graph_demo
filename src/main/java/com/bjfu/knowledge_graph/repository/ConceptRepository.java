package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.ConceptNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConceptRepository extends Neo4jRepository<ConceptNode, Long> {

    /**
     * 根据名称查找概念
     * @param name 概念名称
     * @return 概念对象
     */
    Optional<ConceptNode> findByName(String name);

    /**
     * 检查概念是否被其他节点指向
     * @param name 概念名称
     * @return 是否被指向
     */
    @Query("MATCH (c:Concept {name: $name}) " +
           "RETURN exists((c)<-[]-()) as hasIncoming")
    boolean hasIncomingRelationships(@Param("name") String name);
}
