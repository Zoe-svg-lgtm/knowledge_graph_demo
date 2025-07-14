package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.nodes.layer1.BaseNode;
import com.bjfu.knowledge_graph.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    /**
     * 创建节点，如果同名节点已存在，则不进行任何操作并返回已存在节点的ID。
     * 这是一个更安全的 "findOrCreate" 实现。
     */
    @Override
    @Transactional
    public <T extends BaseNode> Long createNode(T node, Class<T> nodeClass) {
        // 1. 检查节点是否已存在
        Optional<T> existingNode = neo4jTemplate.findOne("MATCH (n) WHERE n.name = $name RETURN n",
                Map.of("name", node.getName()),
                nodeClass);

        if (existingNode.isPresent()) {
            return existingNode.get().getId();
        }

        T savedNode = neo4jTemplate.save(node);
        return savedNode.getId();
    }

    @Override
    public <T extends BaseNode> boolean nodeExists(String name, Class<T> nodeClass) {
        // count 方法是高效的，这部分实现是OK的
        return neo4jTemplate.count("MATCH (n:" + getLabel(nodeClass) + ") WHERE n.name = $name",
                Map.of("name", name)) > 0;
    }

    /**
     * 更新节点。先查找，再更新属性，最后保存。
     * 这种方式更安全，并且可以精确控制哪些属性被更新。
     */
    @Override
    @Transactional
    public <T extends BaseNode> String updateNode(String name, T updatedNode, Class<T> nodeClass) {
        // 1. 查找要更新的节点
        Optional<T> nodeToUpdateOpt = neo4jTemplate.findOne("MATCH (n:" + getLabel(nodeClass) + ") WHERE n.name = $name RETURN n",
                Map.of("name", name),
                nodeClass);

        if (nodeToUpdateOpt.isEmpty()) {
            throw new RuntimeException("Node not found for name: " + name);
        }

        T nodeToUpdate = nodeToUpdateOpt.get();

        // 2. 更新需要修改的属性 (这里以更新所有非ID属性为例)
        updatedNode.setId(nodeToUpdate.getId()); // 将查找到的ID赋给更新对象
        T savedNode = neo4jTemplate.save(updatedNode); // save 方法会根据ID执行更新操作

        return "Node " + savedNode.getId() + " updated successfully";
    }

//    @Override
//    @Transactional
//    public <T extends BaseNode> String deleteNode(String name, Class<T> nodeClass) {
//        // 这部分实现是OK的，使用 execute 执行删除操作很直接
//        String label = getLabel(nodeClass);
//        // 使用 DETACH DELETE 来删除节点及其所有关系
//        String cypher = String.format("MATCH (n:%s {name: $name}) DETACH DELETE n", label);
//        neo4jTemplate.execute(cypher, Map.of("name", name));
//        return "Node with name '" + name + "' deleted successfully";
//    }

    /**
     * 获取实体类对应的 Neo4j 标签（Label）。
     * 这个辅助方法是好的，予以保留。
     */
    private <T> String getLabel(Class<T> nodeClass) {
        Node nodeAnnotation = nodeClass.getAnnotation(Node.class);
        // 检查 nodeAnnotation.value() 是否为空
        if (nodeAnnotation != null && nodeAnnotation.value() != null && nodeAnnotation.value().length > 0 && !nodeAnnotation.value()[0].isEmpty()) {
            return nodeAnnotation.value()[0];
        }
        return nodeClass.getSimpleName();
    }
}