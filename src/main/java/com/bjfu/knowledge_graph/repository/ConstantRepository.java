package com.bjfu.knowledge_graph.repository;

import com.bjfu.knowledge_graph.bean.nodes.layer1.Constant;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConstantRepository extends Neo4jRepository<Constant, Long> {
    /**
     * 根据符号查找常数
     * @param symbol 常数符号
     * @return 常数对象
     */
    Optional<Constant> findBySymbol(String symbol);

    @Query("MATCH (c:Constant {symbol: $symbol}) " +
           "RETURN exists((c)<-[]-()) as hasIncoming")
    boolean hasIncomingRelationships(@Param("symbol") String symbol);

}
