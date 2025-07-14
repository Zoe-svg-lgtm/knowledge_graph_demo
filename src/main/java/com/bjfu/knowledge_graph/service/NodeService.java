package com.bjfu.knowledge_graph.service;

import com.bjfu.knowledge_graph.bean.nodes.layer1.BaseNode;

public interface NodeService {

    /**
     * 创建节点
     * @param node 节点对象，继承自 BaseNode
     * @param nodeClass 节点类型
     * @return 节点 ID
     */
    <T extends BaseNode> Long createNode(T node, Class<T> nodeClass);

    /**
     * 检查节点是否存在
     * @param name 节点的 name 属性（唯一索引）
     * @param nodeClass 节点类型
     * @return 是否存在
     */
    <T extends BaseNode> boolean nodeExists(String name, Class<T> nodeClass);

    /**
     * 更新节点属性
     * @param name 节点的 name 属性（唯一索引）
     * @param updatedNode 更新后的节点对象
     * @param nodeClass 节点类型
     * @return 更新结果
     */
    <T extends BaseNode> String updateNode(String name, T updatedNode, Class<T> nodeClass);

    /**
     * 删除节点
     * @param name 节点的 name 属性（唯一索引）
     * @param nodeClass 节点类型
     * @return 删除结果
     */
    //<T extends BaseNode> String deleteNode(String name, Class<T> nodeClass);
}