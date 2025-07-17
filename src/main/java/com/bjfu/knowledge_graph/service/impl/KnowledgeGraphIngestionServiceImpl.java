package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.dto.FormulaDTO;
import com.bjfu.knowledge_graph.bean.dto.KnowledgeNodeDto;
import com.bjfu.knowledge_graph.bean.nodes.layer1.*;
import com.bjfu.knowledge_graph.bean.relationships.*;
import com.bjfu.knowledge_graph.bean.vo.FormulaVO;
import com.bjfu.knowledge_graph.config.AiConfig;
import com.bjfu.knowledge_graph.repository.*;
import com.bjfu.knowledge_graph.service.KnowledgeGraphIngestionService;
import com.bjfu.knowledge_graph.service.KnowledgeGraphService;
import com.bjfu.knowledge_graph.service.NodeService;
import com.bjfu.knowledge_graph.service.RelationshipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeGraphIngestionServiceImpl implements KnowledgeGraphIngestionService {


    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RelationshipService relationshipService;
    @Autowired
    private TopicNodeRepository topicNodeRepository;
    @Autowired
    private CategoryNodeRepository categoryNodeRepository;
    @Autowired
    private ConceptRepository conceptRepository;
    @Autowired
    private LawNodeRepository lawNodeRepository;
    @Autowired
    private ConditionNodeRepository conditionNodeRepository;
    @Autowired
    private CharacteristicNodeRepository characteristicNodeRepository;
    @Autowired
    private ExampleNodeRepository exampleNodeRepository;
    @Autowired
    private FormulaRepository formulaRepository;
    @Autowired
    private PrincipleNodeRepository principleNodeRepository;
    @Autowired
    private TheoremNodeRepository theoremNodeRepository;
    @Autowired
    private ApplicationNodeRepository applicationNodeRepository;
    @Autowired
    private ConstantRepository constantRepository;
    @Autowired
    private PhysicalQuantityRepository physicalQuantityRepository;

    @Override
    @Transactional
    public void ingestKnowledgeGraph() {
        log.info("开始从JSON文件导入知识图谱数据...");
        try {
            ClassPathResource resource = new ClassPathResource("data/physical.json");
            try (InputStream inputStream = resource.getInputStream()) {
                KnowledgeNodeDto rootDto = objectMapper.readValue(inputStream, KnowledgeNodeDto.class);
                if (rootDto == null) {
                    log.warn("知识图谱根节点DTO为空，无法导入数据。请检查JSON文件是否为空或格式错误。");
                    return;
                }
                processNode(rootDto, null);
                log.info("知识图谱数据导入成功！");
            }
        } catch (IOException e) {
            log.error("加载或解析知识图谱JSON文件失败", e);
            throw new RuntimeException("加载或解析知识图谱JSON文件失败", e);
        }
    }

    private void processNode(KnowledgeNodeDto dto, BaseNode parentNode) {
        if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
            log.warn("检测到空的或无效的DTO节点，已跳过。DTO: {}", dto);
            return;
        }

        BaseNode currentNode = createOrUpdateNode(dto);
        if (currentNode == null) {
            return;
        }

        if (parentNode != null) {
            linkChildToParent(parentNode, currentNode);
        }

        if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
            for (KnowledgeNodeDto childDto : dto.getChildren()) {
                processNode(childDto, currentNode);
            }
        }
    }


    private BaseNode createOrUpdateNode(KnowledgeNodeDto dto) {
        String name = dto.getName();
        Map<String, String> props = dto.getProperties();

        switch (dto.getLabel()) {
            case "Topic":
                TopicNode topic = topicNodeRepository.findByName(name).orElseGet(TopicNode::new);
                topic.setName(name);
                return topicNodeRepository.save(topic);

            case "Category":
                CategoryNode category = categoryNodeRepository.findByName(name).orElseGet(CategoryNode::new);
                category.setName(name);
                return categoryNodeRepository.save(category);

            case "Concept":
                ConceptNode concept = conceptRepository.findByName(name).orElseGet(ConceptNode::new);
                concept.setName(name);
                if (props != null) {
                    concept.setDefinition(props.get("definition"));
                    concept.setText(props.get("text")); // 补充对text属性的处理
                }
                return conceptRepository.save(concept);

            case "Law":
                LawNode law = lawNodeRepository.findByName(name).orElseGet(LawNode::new);
                law.setName(name);
                if (props != null) law.setContent(props.get("content"));
                return lawNodeRepository.save(law);

            case "Condition":
                ConditionNode condition = conditionNodeRepository.findByName(name).orElseGet(ConditionNode::new);
                condition.setName(name);
                if (props != null) condition.setText(props.get("text"));
                return conditionNodeRepository.save(condition);

            case "Characteristic":
                CharacteristicNode characteristic = characteristicNodeRepository.findByName(name).orElseGet(CharacteristicNode::new);
                characteristic.setName(name);
                if (props != null) characteristic.setText(props.get("text"));
                return characteristicNodeRepository.save(characteristic);

            case "Example":
                ExampleNode example = exampleNodeRepository.findByName(name).orElseGet(ExampleNode::new);
                example.setName(name);
                if (props != null) example.setDescription(props.get("description"));
                return exampleNodeRepository.save(example);

            case "Formula":
                Formula formula = formulaRepository.findByName(name).orElseGet(Formula::new);
                formula.setName(props.get("expression"));
                formula.setExpression(name);
                return formulaRepository.save(formula);

            case "Principle":
                PrincipleNode principle = principleNodeRepository.findByName(name).orElseGet(PrincipleNode::new);
                principle.setName(name);
                if (props != null) principle.setText(props.get("text"));
                return principleNodeRepository.save(principle);

            case "Theorem":
                TheoremNode theorem = theoremNodeRepository.findByName(name).orElseGet(TheoremNode::new);
                theorem.setName(name);
                if (props != null) theorem.setContent(props.get("content"));
                return theoremNodeRepository.save(theorem);

            case "Application":
                ApplicationNode application = applicationNodeRepository.findByName(name).orElseGet(ApplicationNode::new);
                application.setName(name);
                if (props != null) application.setDescription(props.get("description"));
                return applicationNodeRepository.save(application);

            default:
                log.error("未知的节点标签: '{}'，无法创建节点。DTO: {}", dto.getLabel(), dto);
                return null;
        }
    }

    private void linkChildToParent(BaseNode parent, BaseNode child) {
        Long parentId = parent.getId();
        Long childId = child.getId();

        if (parentId == null || childId == null) {
            log.error("无法创建关系，因为父节点或子节点的ID为空。Parent: [id={}, name='{}'], Child: [id={}, name='{}']",
                    parentId, parent.getName(), childId, child.getName());
            return;
        }

        // 避免自己指向自己
        if (Objects.equals(parentId, childId)) {
            log.warn("检测到自引用关系，已跳过。Node: [id={}, name='{}']", parentId, parent.getName());
            return;
        }

        if (parent instanceof TopicNode || parent instanceof CategoryNode || parent instanceof ExampleNode || parent instanceof PrincipleNode || parent instanceof TheoremNode) {
            // 这些节点作为父节点时，通常建立通用的HAS_CHILD关系
            relationshipService.createRelationship(parentId, childId, HasChild.class);

        } else if (parent instanceof ConceptNode) {
            // Concept作为父节点，建立更具语义的关系
            if (child instanceof Formula) {
                relationshipService.createRelationship(parentId, childId, HasFormula.class);
            } else if (child instanceof ConditionNode) {
                relationshipService.createRelationship(parentId, childId, HasCondition.class);
            } else if (child instanceof CharacteristicNode) {
                relationshipService.createRelationship(parentId, childId, HasCharacteristic.class);
            } else if (child instanceof ExampleNode) {
                relationshipService.createRelationship(parentId, childId, HasExample.class);
            } else if (child instanceof ConceptNode) {
                relationshipService.createRelationship(parentId, childId, HasSubconcept.class);
            } else {
                relationshipService.createRelationship(parentId, childId, HasChild.class);
            }

        } else if (parent instanceof LawNode) {
            // Law作为父节点，建立更具语义的关系
            if (child instanceof Formula) {
                relationshipService.createRelationship(parentId, childId, HasFormula.class);
            } else if (child instanceof ConditionNode) {
                relationshipService.createRelationship(parentId, childId, HasCondition.class);
            } else if (child instanceof ExampleNode) {
                relationshipService.createRelationship(parentId, childId, HasExample.class);
            } else if (child instanceof ApplicationNode) { // 补充对ApplicationNode的处理
                relationshipService.createRelationship(parentId, childId, HasApplication.class); // 假设已创建HasApplication关系实体
            } else {
                relationshipService.createRelationship(parentId, childId, HasChild.class);
            }
        } else {
            log.warn("未处理的父节点类型 '{}'，将默认创建HAS_CHILD关系。", parent.getClass().getSimpleName());
            relationshipService.createRelationship(parentId, childId, HasChild.class);
        }
    }
}
