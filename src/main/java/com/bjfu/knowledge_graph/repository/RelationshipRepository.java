package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.relationships.BaseRelationship;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface RelationshipRepository extends Neo4jRepository<BaseRelationship, Long> {
    
    @Query("MATCH (start)-[r:$relationshipType]->(end) " +
           "WHERE elementId(start) = $startId AND elementId(end) = $endId " +
           "RETURN count(r) > 0")
    boolean existsRelationship(Long startId, Long endId, String relationshipType);


    /**
     * 删除CONTAINS_CONSTANT_RELATIONSHIP关系
     * @param startId 起始节点ID
     * @param endId 结束节点ID
     * @return 删除的关系数量
     */
    @Query("MATCH (start)-[r:CONTAINS_CONSTANT_RELATIONSHIP]->(end) " +
            "WHERE elementId(start) = $startId AND elementId(end) = $endId " +
            "DELETE r RETURN count(r) as deletedCount")
    Integer deleteContainsConstantRelationship(@Param("startId")Long startId, @Param("endId")Long endId);

    /**
     * 删除 CONTAINS_QUANTITY_RELATIONSHIP 关系
     * @param startId 起始节点ID
     * @param endId 结束节点ID
     * @return 删除的关系数量
     */
    @Query("MATCH (start)-[r:CONTAINS_QUANTITY_RELATIONSHIP]->(end) " +
            "WHERE elementId(start) = $startId AND elementId(end) = $endId " +
            "DELETE r RETURN count(r) as deletedCount")
    Integer deleteContainsQuantityRelationship(@Param("startId")Long startId, @Param("endId")Long endId);


}