package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.TheoremNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TheoremNode (定理节点) 的数据访问接口。
 */
@Repository
public interface TheoremNodeRepository extends Neo4jRepository<TheoremNode, Long> {

    /**
     * 根据定理的名称查找节点。
     * name 属性被视为唯一标识符，用于防止重复创建。
     *
     * @param name 定理的名称，例如 "动能定理"
     * @return 包含找到的TheoremNode的Optional，如果未找到则为空
     */
    Optional<TheoremNode> findByName(String name);
}