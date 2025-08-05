package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface FormulaRepository extends Neo4jRepository<Formula, Long> {
    /**
     * 根据公式名称查找公式
     * @param name 公式名称
     * @return 公式对象
     */
    Optional<Formula> findByName(String name);
    /**
     * 检查公式名称是否存在
     * @param name 公式名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 查找包含指定物理量的所有公式
     * @param quantityId 物理量ID
     * @return 包含该物理量的公式列表
     */
    @Query("MATCH (f:Formula)-[:CONTAINS_QUANTITY_RELATIONSHIP]->(q:PhysicalQuantity) " +
            "WHERE id(q) = $quantityId " +
            "WITH f " +
            // 匹配并收集与 f 相关的 PhysicalQuantity
            "MATCH (f)-[r1:CONTAINS_QUANTITY_RELATIONSHIP]->(quantities:PhysicalQuantity) " +
            // 使用 OPTIONAL MATCH 来匹配常量，因为一个公式可能没有常量
            "OPTIONAL MATCH (f)-[r2:CONTAINS_CONSTANT_RELATIONSHIP]->(constants:Constant) " +
            // 返回 f 以及它所有相关的关系和节点集合
            "RETURN f, COLLECT(DISTINCT r1), COLLECT(DISTINCT quantities), COLLECT(DISTINCT r2), COLLECT(DISTINCT constants)")
    List<Formula> findFormulasContainingQuantity(@Param("quantityId") Long quantityId);
    /**
     * 检查公式是否存在，并且所有需要的物理量和常数关系都存在
     * @param formulaName 公式名称
     * @param quantitySymbols 需要的物理量符号列表
     * @param constantSymbols 需要的常数符号列表
     * @return 是否存在满足条件的公式
     */
    @Query("MATCH (f:Formula {name: $name}) " +
            "WHERE " +
            "  // 检查是否所有需要的物理量关系都存在 " +
            "  ALL(q_symbol IN $quantitySymbols WHERE EXISTS((f)-[:CONTAINS_QUANTITY]->(:PhysicalQuantity {symbol: q_symbol}))) " +
            "  AND " +
            "  // 检查是否所有需要的常数关系都存在 " +
            "  ALL(c_symbol IN $constantSymbols WHERE EXISTS((f)-[:CONTAINS_CONSTANT]->(:Constant {symbol: c_symbol}))) " +
            "RETURN COUNT(f) > 0")
    boolean formulaAndAllRelationshipsExist(String formulaName, List<String> quantitySymbols, List<String> constantSymbols);

}