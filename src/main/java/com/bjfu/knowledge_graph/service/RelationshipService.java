package com.bjfu.knowledge_graph.service;

import com.bjfu.knowledge_graph.bean.relationships.BaseRelationship;

public interface RelationshipService {
    
    /**
     * 创建关系的通用方法
     * @param startNodeId 起始节点ID
     * @param endNodeId 结束节点ID
     * @param relationshipClass 关系类型
     * @return 创建结果
     */
    <T extends BaseRelationship<?, ?>> String createRelationship(
        Long startNodeId, 
        Long endNodeId, 
        Class<T> relationshipClass
    );

    
    /**
     * 删除关系
     */
    <T extends BaseRelationship<?, ?>> String deleteRelationship(
        Long startNodeId, 
        Long endNodeId, 
        Class<T> relationshipClass
    );
}