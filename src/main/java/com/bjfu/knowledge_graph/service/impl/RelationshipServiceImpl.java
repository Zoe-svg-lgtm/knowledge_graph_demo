package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.relationships.BaseRelationship;
import com.bjfu.knowledge_graph.service.RelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class RelationshipServiceImpl implements RelationshipService {

    @Autowired
    private Neo4jTemplate neo4jTemplate;

    // 关系类型映射
    private static final Map<Class<?>, String> RELATIONSHIP_TYPE_MAP = new HashMap<>();

    public RelationshipServiceImpl() {
        initializeRelationshipTypeMapWithSpring();
    }

    private void initializeRelationshipTypeMapWithSpring() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        // 添加过滤器：只扫描 BaseRelationship 的子类
        scanner.addIncludeFilter(new AssignableTypeFilter(BaseRelationship.class));

        // 扫描指定包
        Set<BeanDefinition> candidateComponents =
                scanner.findCandidateComponents("com.bjfu.knowledge_graph.domain.relationships");

        for (BeanDefinition beanDefinition : candidateComponents) {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());

                // 排除抽象类和 BaseRelationship 本身
                if (!clazz.equals(BaseRelationship.class) &&
                        !Modifier.isAbstract(clazz.getModifiers())) {

                    String relationshipType = extractRelationshipType(clazz);
                    RELATIONSHIP_TYPE_MAP.put(clazz, relationshipType);

                    log.debug("注册关系类型: {} -> {}", clazz.getSimpleName(), relationshipType);
                }

            } catch (ClassNotFoundException e) {
                log.warn("无法加载类: {}", beanDefinition.getBeanClassName());
            }
        }

        log.info("自动扫描到 {} 个关系类型", RELATIONSHIP_TYPE_MAP.size());
    }

    private String extractRelationshipType(Class<?> clazz) {
        // 首先检查是否有 @Relationship 注解
        org.springframework.data.neo4j.core.schema.Relationship relationshipAnnotation =
                clazz.getAnnotation(org.springframework.data.neo4j.core.schema.Relationship.class);
        if (relationshipAnnotation != null && !relationshipAnnotation.type().isEmpty()) {
            return relationshipAnnotation.type();
        }

        RelationshipProperties propertiesAnnotation = clazz.getAnnotation(RelationshipProperties.class);
        if (propertiesAnnotation != null) {
            String className = clazz.getSimpleName();
            if (className.endsWith("Properties")) {
                className = className.substring(0, className.length() - "Properties".length());
            }
            return camelCaseToUpperSnakeCase(className);
        }

        // 从类名转换
        return camelCaseToUpperSnakeCase(clazz.getSimpleName());
    }

    private String camelCaseToUpperSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }


    @Override
    @Transactional
    public <T extends BaseRelationship<?, ?>> String createRelationship(
            Long startNodeId,
            Long endNodeId,
            Class<T> relationshipClass) {

        try {
            // 1. 获取关系类型名称
            String relationshipType = RELATIONSHIP_TYPE_MAP.get(relationshipClass);
            if (relationshipType == null) {
                log.error("不支持的关系类型: {}", relationshipClass.getSimpleName());
                return "不支持的关系类型: " + relationshipClass.getSimpleName();
            }

            // 2. 使用 MERGE 原子地创建关系
            String cypher = String.format(
                    "MATCH (start) WHERE id(start) = $startId " +
                            "MATCH (end) WHERE id(end) = $endNodeId " +
                            "MERGE (start)-[r:`%s`]->(end) " +
                            "RETURN count(r)",
                    relationshipType
            );

            Map<String, Object> params = Map.of(
                    "startId", startNodeId,
                    "endNodeId", endNodeId
            );

            long count = neo4jTemplate.count(cypher, params);

            if (count > 0) {
                log.info("成功创建或匹配关系: {} -> {} ({})", startNodeId, endNodeId, relationshipType);
                return "成功创建或匹配关系: " + relationshipType;
            } else {
                log.warn("无法创建关系，可能是因为起始节点或结束节点不存在: startId={}, endId={}", startNodeId, endNodeId);
                return "创建关系失败：起始或结束节点不存在";
            }

        } catch (Exception e) {
            log.error("创建关系失败", e);
            return "创建关系失败: " + e.getMessage();
        }
    }



    @Override
    @Transactional
    public <T extends BaseRelationship<?, ?>> String deleteRelationship(
            Long startNodeId,
            Long endNodeId,
            Class<T> relationshipClass) {

        try {
            String relationshipType = RELATIONSHIP_TYPE_MAP.get(relationshipClass);
            if (relationshipType == null) {
                return "不支持的关系类型: " + relationshipClass.getSimpleName();
            }

            String cypher = String.format(
                    "MATCH (start)-[r:%s]->(end) WHERE id(start) = $startId AND id(end) = $endId DELETE r RETURN count(r) as deletedCount",
                    relationshipType
            );

            Map<String, Object> params = Map.of(
                    "startId", startNodeId,
                    "endId", endNodeId
            );

            Integer deletedCount = neo4jTemplate.findOne(cypher, params, Integer.class).orElse(0);

            if (deletedCount > 0) {
                log.info("成功删除关系: {} -> {} ({})", startNodeId, endNodeId, relationshipType);
                return "成功删除关系: " + relationshipType;
            } else {
                return "未找到要删除的关系";
            }

        } catch (Exception e) {
            log.error("删除关系失败", e);
            return "删除关系失败: " + e.getMessage();
        }
    }

    private Class<?> getStartNodeClass(Class<?> relationshipClass) {
        return getGenericType(relationshipClass, 0);
    }

    private Class<?> getEndNodeClass(Class<?> relationshipClass) {
        return getGenericType(relationshipClass, 1);
    }

    /**
     * 获取关系类的泛型类型
     * @param relationshipClass 关系类
     * @param index 泛型参数索引，0表示起始节点类型，1表示结束节点类型
     * @return 泛型类型
     */
    private Class<?> getGenericType(Class<?> relationshipClass, int index) {
        Type genericSuperclass = relationshipClass.getGenericSuperclass();

        // 如果直接继承自 BaseRelationship，检查泛型参数
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > index) {
                return (Class<?>) actualTypeArguments[index];
            }
        }

        // 如果是多级继承，递归查找
        Class<?> currentClass = relationshipClass;
        while (currentClass != null && currentClass != BaseRelationship.class) {
            Type[] genericInterfaces = currentClass.getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    if (parameterizedType.getRawType() == BaseRelationship.class) {
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        if (actualTypeArguments.length > index) {
                            return (Class<?>) actualTypeArguments[index];
                        }
                    }
                }
            }

            genericSuperclass = currentClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > index) {
                    return (Class<?>) actualTypeArguments[index];
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        throw new IllegalArgumentException("无法获取泛型类型，请确保关系类正确继承了BaseRelationship并指定了泛型参数");
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseRelationship<?, ?>> T createRelationshipInstance(
            Class<T> relationshipClass, Object startNode, Object endNode) throws Exception {

        Class<?> startNodeClass = getStartNodeClass(relationshipClass);
        Class<?> endNodeClass = getEndNodeClass(relationshipClass);

        try {
            Constructor<T> constructor = relationshipClass.getConstructor(startNodeClass, endNodeClass);
            return constructor.newInstance(startNode, endNode);
        } catch (NoSuchMethodException ex) {
            log.warn("类 {} 中找不到公开的 (start, end) 节点构造器，将回退到无参构造器和setter方法。 " +
                            "强烈建议为所有关系实体提供一个 (StartNode, EndNode) 构造器以提升效率和健壮性。",
                    relationshipClass.getSimpleName());
        }

        T relationship;
        try {
            Constructor<T> constructor = relationshipClass.getDeclaredConstructor();
            relationship = constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("关系类 " + relationshipClass.getSimpleName() +
                    " 必须有一个公开的无参构造器，或者一个公开的(StartNode, EndNode)构造器。", e);
        }

        try {
            Method setStartNodeMethod = findMethod(relationshipClass, "setStartNode", startNodeClass);
            Method setEndNodeMethod = findMethod(relationshipClass, "setEndNode", endNodeClass);

            setStartNodeMethod.invoke(relationship, startNode);
            setEndNodeMethod.invoke(relationship, endNode);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "无法通过setter方法设置关系节点。请检查 " + relationshipClass.getSimpleName() +
                            " (或其父类) 是否有正确的 public setter 方法 (例如 setStartNode)，并且 Lombok 是否已正确配置和启用。", e);
        }

        return relationship;
    }

    private Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            Class<?> currentClass = clazz;
            while (currentClass != null && currentClass != Object.class) {
                try {
                    Method method = currentClass.getDeclaredMethod(methodName, paramTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ex) {
                }
                currentClass = currentClass.getSuperclass();
            }
            throw e;
        }
    }
}