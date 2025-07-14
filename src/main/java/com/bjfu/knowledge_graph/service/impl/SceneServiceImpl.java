package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.bean.dto.FormulaDTO;
import com.bjfu.knowledge_graph.bean.dto.SceneDTO;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.bean.nodes.layer2.Scenario;
import com.bjfu.knowledge_graph.bean.relationships.CoreFormula;
import com.bjfu.knowledge_graph.repository.FormulaRepository;
import com.bjfu.knowledge_graph.repository.SceneRepository;
import com.bjfu.knowledge_graph.service.RelationshipService;
import com.bjfu.knowledge_graph.service.SceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SceneServiceImpl implements SceneService {

    @Autowired
    private FormulaRepository formulaRepository;

    @Autowired
    private SceneRepository sceneRepository;

    @Autowired
    private KnowledgeGraphServiceImpl knowledgeGraphService;

    @Autowired
    private RelationshipService relationshipService;

    @Override
    @Transactional
    public Scenario addScene(SceneDTO sceneDTO) {
        // 验证场景的必要信息是否存在
        if (sceneDTO.getName() == null || sceneDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("场景名称不能为空");
        }

        // 检查场景是否已存在
        if (sceneRepository.existsByScenarioName(sceneDTO.getName())) {
            throw new IllegalStateException("场景 '" + sceneDTO.getName() + "' 已存在。");
        }

        // 创建场景节点
        Scenario scenario = new Scenario().builder()
                .scenarioName(sceneDTO.getName())
                .description(sceneDTO.getDescription())
                .keywords(sceneDTO.getKeywords())
                .difficulty(sceneDTO.getDifficulty())
                .build();

        // 保存场景节点到数据库
        Scenario savedScenario = sceneRepository.save(scenario);

        // 处理核心公式关系
        if (sceneDTO.getCoreFormulas() != null && !sceneDTO.getCoreFormulas().isEmpty()) {
            List<Formula> coreFormulas = new ArrayList<>();

            for (String coreFormulaName : sceneDTO.getCoreFormulas()) {
                if (coreFormulaName == null || coreFormulaName.trim().isEmpty()) {
                    continue; // 跳过空的公式名称
                }

                Formula formula;
                // 检查公式是否存在，不存在则创建
                if (!formulaRepository.existsByName(coreFormulaName)) {
                    // 创建公式
                    FormulaDTO formulaDTO = new FormulaDTO();
                    formulaDTO.setFormula(coreFormulaName);
                    formula = knowledgeGraphService.addFormula(formulaDTO);
                } else {
                    // 查询已存在的公式
                    formula = formulaRepository.findByName(coreFormulaName)
                            .orElseThrow(() -> new RuntimeException("公式查询失败: " + coreFormulaName));
                }

                coreFormulas.add(formula);

                // 创建场景和公式之间的关系
                relationshipService.createRelationship(
                        savedScenario.getId(),
                        formula.getId(),
                        CoreFormula.class
                );
            }
            // 设置场景的核心公式列表
            savedScenario.setCoreFormulas(coreFormulas);
        }
        // 处理父场景关系（如果有）
//        if (sceneDTO.getParentScenarioName() != null && !sceneDTO.getParentScenarioName().trim().isEmpty()) {
//            Scenario parentScenario = sceneRepository.findByScenarioName(sceneDTO.getParentScenarioName())
//                    .orElseThrow(() -> new RuntimeException("父场景不存在: " + sceneDTO.getParentScenarioName()));
//
//            savedScenario.setParentScenario(parentScenario);
//            // 这里可能需要创建 IS_A_TYPE_OF 关系，具体取决于你的 relationshipService 实现
//        }

        return savedScenario;
    }

}