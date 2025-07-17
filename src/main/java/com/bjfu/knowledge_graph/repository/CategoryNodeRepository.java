package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.CategoryNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryNodeRepository extends Neo4jRepository<CategoryNode, Long> {

    /**
     * 根据名称查找分类节点
     * @param name 分类名称
     * @return 分类节点
     */
    Optional<CategoryNode> findByName(String name);

    /**
     * 检查分类节点是否被其他节点指向
     * @param name 分类名称
     * @return 是否被指向
     */
    @Query("MATCH (c:CategoryNode {name: $name}) " +
           "RETURN exists((c)<-[]-()) as hasIncoming")
    boolean hasIncomingRelationships(@Param("name") String name);
}
