package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.nodes.layer1.BaseNode;
import com.bjfu.knowledge_graph.service.NodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class NodeServiceImpl implements NodeService {

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    @Override
    @Transactional
    public <T extends BaseNode> Long createNode(T node, Class<T> nodeClass) {
        // 1. 从 Class 对象获取节点标签
        org.springframework.data.neo4j.core.schema.Node nodeAnnotation = nodeClass.getAnnotation(org.springframework.data.neo4j.core.schema.Node.class);
        if (nodeAnnotation == null) {
            throw new IllegalArgumentException("Class " + nodeClass.getName() + " is not a valid Neo4j @Node entity.");
        }
        // 如果@Node注解没有指定value，则默认使用类名作为标签
        String label = nodeAnnotation.value().length > 0 ? nodeAnnotation.value()[0] : nodeClass.getSimpleName();

        // 2. 构造正确的、带标签的查询语句
        String cypherQuery = String.format("MATCH (n:`%s`) WHERE n.name = $name RETURN n", label);

        // 3. 使用修正后的查询来检查节点是否存在
        Optional<T> existingNode = neo4jTemplate.findOne(cypherQuery,
                Map.of("name", node.getName()),
                nodeClass);

        if (existingNode.isPresent()) {
            log.info("节点 '{}' (标签: {}) 已存在，ID: {}", node.getName(), label, existingNode.get().getId());
            return existingNode.get().getId();
        }

        // 4. 保存新节点
        // 即使修复了查询，这里的 save 仍然是潜在的风险点。
        // 更稳妥的方式是放弃这个通用方法，使用特定Repository。
        try {
            log.info("节点 '{}' (标签: {}) 不存在, 创建新节点.", node.getName(), label);
            T savedNode = neo4jTemplate.save(node); // 尝试保存
            if (savedNode == null || savedNode.getId() == null) {
                // 这里可以做一个额外的查询来验证是否创建成功并获取ID，作为备用方案
                log.error("neo4jTemplate.save()未能返回带ID的节点。尝试重新获取...");
                // 重新查询一次，这次它肯定存在了
                T freshlyFetchedNode = neo4jTemplate.findOne(cypherQuery, Map.of("name", node.getName()), nodeClass)
                        .orElseThrow(() -> new IllegalStateException("节点创建失败，保存后仍无法找到: " + node.getName()));
                return freshlyFetchedNode.getId();
            }
            log.info("成功创建节点 '{}' (标签: {}), ID: {}", savedNode.getName(), label, savedNode.getId());
            return savedNode.getId();
        } catch (Exception e) {
            log.error("保存节点 '{}' 失败: {}", node.getName(), e.getMessage(), e);
            throw new IllegalStateException("保存节点失败: " + e.getMessage(), e);
        }
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