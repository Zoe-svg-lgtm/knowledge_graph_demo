package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.PrincipleNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PrincipleNode (原理节点) 的数据访问接口。
 */
@Repository
public interface PrincipleNodeRepository extends Neo4jRepository<PrincipleNode, Long> {

    /**
     * 根据原理的名称查找节点。
     * name 属性被视为唯一标识符，用于防止重复创建。
     *
     * @param name 原理的名称，例如 "功能原理"
     * @return 包含找到的PrincipleNode的Optional，如果未找到则为空
     */
    Optional<PrincipleNode> findByName(String name);
}