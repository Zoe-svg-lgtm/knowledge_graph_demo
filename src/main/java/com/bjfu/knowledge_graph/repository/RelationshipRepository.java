package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.relationships.BaseRelationship;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface RelationshipRepository extends Neo4jRepository<BaseRelationship, Long> {
    
    @Query("MATCH (start)-[r:$relationshipType]->(end) " +
           "WHERE id(start) = $startId AND id(end) = $endId " +
           "RETURN count(r) > 0")
    boolean existsRelationship(Long startId, Long endId, String relationshipType);
}