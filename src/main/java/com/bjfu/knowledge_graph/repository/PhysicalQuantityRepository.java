package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhysicalQuantityRepository extends Neo4jRepository<PhysicalQuantity, Long> {

    /**
     * 根据符号查找物理量
     * @param symbol 物理量符号
     * @return 物理量对象
     */
    Optional<PhysicalQuantity> findBySymbol(String symbol);

    /**
     * 根据公式ID查找相关的物理量
     * 注意：这个方法需要自定义查询，因为Neo4j无法直接通过formulaId属性查找
     * 假设PhysicalQuantity和Formula之间存在关系
     * @param formulaId 公式ID
     * @return 物理量列表
     */
    @Query("MATCH (pq:PhysicalQuantity)-[:APPEARS_IN]->(f:Formula) WHERE ID(f) = $formulaId RETURN pq")
    List<PhysicalQuantity> findByFormulaId(@Param("formulaId") Long formulaId);

    /**
     * 根据物理量名称查找物理量节点ID
     * @param name 物理量名称
     * @return 物理量节点ID
     */
    @Query("MATCH (pq:PhysicalQuantity {name: $name}) RETURN toString(ID(pq))")
    String findQuantityNodeIdByName(@Param("name") String name);

    /**
     * 检查物理量是否被其他节点指向
     * @param symbol 物理量符号
     * @return 是否被指向
     */
    @Query("MATCH (pq:PhysicalQuantity {symbol: $symbol}) " +
            "RETURN exists((pq)<-[]-()) as hasIncoming")
    boolean hasIncomingRelationships(@Param("symbol") String symbol);





    /**
     * 如果PhysicalQuantity实体中直接有formulaId属性，可以使用这个方法
     * @param formulaId 公式ID
     * @return 物理量列表
     */
//    List<PhysicalQuantity> findByFormulaId(Long formulaId);
}