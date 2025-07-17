package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.ConditionNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConditionNodeRepository extends Neo4jRepository<ConditionNode, Long> {

    /**
     * 根据名称查找条件节点
     * @param name 条件名称
     * @return 条件节点
     */
    Optional<ConditionNode> findByName(String name);

    /**
     * 检查条件节点是否被其他节点指向
     * @param name 条件名称
     * @return 是否被指向
     */
    @Query("MATCH (c:ConditionNode {name: $name}) " +
           "RETURN exists((c)<-[]-()) as hasIncoming")
    boolean hasIncomingRelationships(@Param("name") String name);
}
