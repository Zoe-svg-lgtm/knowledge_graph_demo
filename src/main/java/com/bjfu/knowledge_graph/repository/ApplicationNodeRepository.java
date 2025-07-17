package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.ApplicationNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ApplicationNode (应用节点) 的数据访问接口。
 */
@Repository
public interface ApplicationNodeRepository extends Neo4jRepository<ApplicationNode, Long> {

    /**
     * 根据应用的名称查找节点。
     * name 属性被视为唯一标识符，用于防止重复创建。
     *
     * @param name 应用的名称，例如 "超重、失重问题"
     * @return 包含找到的ApplicationNode的Optional，如果未找到则为空
     */
    Optional<ApplicationNode> findByName(String name);
}