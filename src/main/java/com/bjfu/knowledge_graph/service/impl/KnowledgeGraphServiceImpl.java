package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.config.AiConfig;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Constant;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.bean.nodes.layer1.PhysicalQuantity;
import com.bjfu.knowledge_graph.bean.relationships.ContainsConstantRelationship;
import com.bjfu.knowledge_graph.bean.relationships.ContainsQuantityRelationship;
import com.bjfu.knowledge_graph.bean.dto.FormulaDTO;
import com.bjfu.knowledge_graph.repository.ConstantRepository;
import com.bjfu.knowledge_graph.service.NodeService; // 引入 NodeService
import com.bjfu.knowledge_graph.service.RelationshipService;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import com.bjfu.knowledge_graph.bean.vo.FormulaVO;
import com.bjfu.knowledge_graph.repository.FormulaRepository;
import com.bjfu.knowledge_graph.repository.PhysicalQuantityRepository;
import com.bjfu.knowledge_graph.service.KnowledgeGraphService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class KnowledgeGraphServiceImpl implements KnowledgeGraphService {

    // 保留 Repository 用于复杂的、非通用的查询
    @Autowired
    private FormulaRepository formulaRepository;
    @Autowired
    private PhysicalQuantityRepository physicalQuantityRepository;
    @Autowired
    private ConstantRepository constantRepository;

    // 引入通用服务
    @Autowired
    private NodeService nodeService;
    @Autowired
    private RelationshipService relationshipService;

    // 其他依赖
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AiConfig.FormulaAnalysisService formulaAnalysisService;

    @Override
    @Transactional // 确保整个方法是一个原子操作
    public Formula addFormula(FormulaDTO formulaDTO) {

        log.info("开始分析公式: {}", formulaDTO.getFormula());
        String analysisResult = formulaAnalysisService.analyzeFormula(formulaDTO.getFormula(), formulaDTO.getContext());
        log.info("从LLM获取的原始分析结果: \n---\n{}\n---", analysisResult);
        if (analysisResult.contains("{") && analysisResult.contains("}")) {
            analysisResult = analysisResult.substring(analysisResult.indexOf('{'), analysisResult.lastIndexOf('}') + 1);
        }

        FormulaVO resultVO;
        try {
            resultVO = objectMapper.readValue(analysisResult, FormulaVO.class);
        } catch (Exception e) {
            throw new RuntimeException("无法解析AI模型的分析结果: " + e.getMessage(), e);
        }

        // 2. 提取用于检查的符号列表
        List<String> quantitySymbols = resultVO.getQuantities().stream()
                .map(PhysicalQuantity::getSymbol)
                .collect(Collectors.toList());

        List<String> constantSymbols = resultVO.getConstants().stream()
                .map(Constant::getSymbol)
                .collect(Collectors.toList());

        // 3. 检查公式及所有关系是否已完全存在
        if (formulaRepository.findByName(resultVO.getFormulaName()).isPresent()) {
            log.info("公式 '{}' 已存在，无需操作。", resultVO.getFormulaName());
            return null;
        }

        // 4. 创建或获取物理量节点
        Set<PhysicalQuantity> quantities = resultVO.getQuantities().stream()
                .map(this::getOrCreatePhysicalQuantity)
                .collect(Collectors.toSet());

        // 5. 创建或获取常数节点
        Set<Constant> constants = resultVO.getConstants().stream()
                .map(this::getOrCreateConstant)
                .collect(Collectors.toSet());

        // 6. 创建公式节点
        Formula formula = formulaRepository.findByName(resultVO.getFormulaName())
                .orElseGet(() -> {
                    Formula newFormula = new Formula();
                    newFormula.setName(resultVO.getFormulaName());
                    newFormula.setExpression(resultVO.getExpression());
                    Long formulaId = nodeService.createNode(newFormula, Formula.class);
                    newFormula.setId(formulaId);
                    return newFormula;
                });

        if (formula.getId() == null) {
            throw new IllegalStateException("无法获取公式节点的ID，操作中止。");
        }

        createFormulaRelationships(formula, quantities, constants);

        log.info("成功添加公式: {}", formula.getName());
        return formula;
    }


    /**
     * 删除公式及其关系
     */
    @Override
    @Transactional
    public ReturnMsg deleteFormula(Long formulaId) {
        Optional<Formula> formulaOpt = formulaRepository.findById(formulaId);
        if (formulaOpt.isEmpty()) {
            return ReturnMsg.fail(ReturnMsg.ERROR, "公式不存在或已被删除");
        }

        Formula formula = formulaOpt.get();
        String formulaName = formula.getName();
        // 删除公式的话要删除公式以及关联的物理量和常数关系
        try {
            // 删除关系
            if (!formula.getQuantities().isEmpty()) {
                for (PhysicalQuantity quantity : formula.getQuantities()) {
                    relationshipService.deleteRelationship(formulaId, quantity.getId(), ContainsQuantityRelationship.class);
                    if (physicalQuantityRepository.hasIncomingRelationships(quantity.getSymbol())) {
                        log.info("物理量 '{}' 仍被其他公式引用，跳过删除。", quantity.getName());
                    } else {
                        physicalQuantityRepository.deleteById(quantity.getId());
                        log.info("成功删除物理量: {}", quantity.getName());
                    }
                }
            }

            if (!formula.getConstants().isEmpty()) {
                for (Constant constant : formula.getConstants()) {
                    relationshipService.deleteRelationship(formulaId, constant.getId(), ContainsConstantRelationship.class);
                    if (constantRepository.hasIncomingRelationships(constant.getSymbol())) {
                        log.info("常数 '{}' 仍被其他公式引用，跳过删除。", constant.getName());
                    } else {
                        constantRepository.deleteById(constant.getId());
                        log.info("成功删除常数: {}", constant.getName());
                    }
                }
            }
            //删除公式
            formulaRepository.deleteById(formulaId);
            log.info("成功删除公式: {}", formulaName);
            return ReturnMsg.success(ReturnMsg.SUCCESS, "删除公式成功");

        } catch (Exception e) {
            log.error("删除公式失败", e);
            return ReturnMsg.fail(ReturnMsg.ERROR, "删除公式失败: " + e.getMessage());
        }
    }

    /**
     * 根据公式名称获取公式详情
     *
     * @param formulaId 公式id
     * @return 公式视图对象
     */
    @Override
    public FormulaVO getFormulaById(Long formulaId) {
        Optional<Formula> formulaOpt = formulaRepository.findById(formulaId);
        if (formulaOpt.isPresent()) {
            Formula formula = formulaOpt.get();
            FormulaVO formulaVO = new FormulaVO();
            formulaVO.setFormulaName(formula.getName());
            formulaVO.setExpression(formula.getExpression());
            formulaVO.setQuantities(formula.getQuantities());
            formulaVO.setConstants(formula.getConstants());
            return formulaVO;
        }
        return null;
    }

    /**
     * 将图谱中没有分析的公式进行分析
     */
    @Override
    @Transactional
    public void initFormulas() {
        // 获取所有未分析的公式
        List<Formula> formulas = formulaRepository.findAll().stream()
                .filter(formula -> formula.getQuantities().isEmpty() && formula.getQuantities().isEmpty())
                .collect(Collectors.toList());

        if (formulas.isEmpty()) {
            log.info("没有需要分析的公式");
            return;
        }
        log.info("开始分析 {} 个未分析的公式", formulas.size());
        for (Formula formula : formulas) {
            try {
                log.info("分析公式: {}", formula.getName());
                String analysisResult = formulaAnalysisService.analyzeFormula(formula.getExpression(), formula.getName());
                log.info("从LLM获取的原始分析结果: \n---\n{}\n---", analysisResult);

                if (analysisResult.contains("{") && analysisResult.contains("}")) {
                    analysisResult = analysisResult.substring(analysisResult.indexOf('{'), analysisResult.lastIndexOf('}') + 1);
                }

                FormulaVO resultVO = objectMapper.readValue(analysisResult, FormulaVO.class);

                // 创建或获取物理量节点
                Set<PhysicalQuantity> quantities = resultVO.getQuantities().stream()
                        .map(this::getOrCreatePhysicalQuantity)
                        .collect(Collectors.toSet());

                // 创建或获取常数节点
                Set<Constant> constants = resultVO.getConstants().stream()
                        .map(this::getOrCreateConstant)
                        .collect(Collectors.toSet());

                // 创建公式与物理量、常数的关系
                createFormulaRelationships(formula, quantities, constants);

            } catch (Exception e) {
                log.error("分析公式 '{}' 失败: {}", formula.getName(), e.getMessage(), e);
            }
        }

    }

    /**
     * 获取或创建物理量
     */
    private PhysicalQuantity getOrCreatePhysicalQuantity(PhysicalQuantity quantityInfo) {
        return physicalQuantityRepository.findBySymbol(quantityInfo.getSymbol())
                .orElseGet(() -> {
                    // 如果找不到，则创建并保存新节点
                    log.info("物理量 '{}' (Symbol: {}) 不存在, 开始创建新节点.", quantityInfo.getName(), quantityInfo.getSymbol());
                    // 使用 repository.save()，它会返回带有ID的已保存实体
                    PhysicalQuantity savedQuantity = physicalQuantityRepository.save(quantityInfo);
                    log.info("成功创建物理量 '{}', 数据库ID: {}", savedQuantity.getName(), savedQuantity.getId());
                    return savedQuantity;
                });
    }

    /**
     * 获取或创建常数
     */
    private Constant getOrCreateConstant(Constant constantInfo) {
        return constantRepository.findBySymbol(constantInfo.getSymbol())
                .orElseGet(() -> {
                    log.info("常数 '{}' (Symbol: {}) 不存在, 创建新节点.", constantInfo.getName(), constantInfo.getSymbol());
                    // 使用 repository.save()，它会返回带有ID的已保存实体
                    Constant savedConstant = constantRepository.save(constantInfo);
                    log.info("成功创建常数 '{}', 数据库ID: {}", savedConstant.getName(), savedConstant.getId());
                    return savedConstant;
                });
    }

    /**
     * 创建公式与物理量、常数的关系
     */
    private void createFormulaRelationships(Formula formula, Set<PhysicalQuantity> quantities, Set<Constant> constants) {
        if(formula.getId() == null){
            log.info("公式{}不存在或获取不到id", formula.getName());
            throw new IllegalStateException("无法获取公式{}节点的ID，操作中止。");
        }
        // 创建公式与物理量的关系
        for (PhysicalQuantity quantity : quantities) {
            if(quantity.getId() == null){
                log.info("物理量{}不存在或获取不到id", quantity.getName());
                throw new IllegalStateException("无法获取物理量{}节点的ID，操作中止。");
            }
            relationshipService.createRelationship(
                    formula.getId(),
                    quantity.getId(),
                    ContainsQuantityRelationship.class
            );
        }

        // 创建公式与常数的关系
        for (Constant constant : constants) {
            if(constant.getId() == null){
                log.info("常数{}不存在或获取不到id", constant.getName());
                throw new IllegalStateException("无法获取常数{}节点的ID，操作中止。");
            }
            relationshipService.createRelationship(
                    formula.getId(),
                    constant.getId(),
                    ContainsConstantRelationship.class
            );
        }
    }


    @Override
    public List<FormulaVO> getAllFormulas() {
        List<Formula> formulaList = formulaRepository.findAll();
        if (formulaList.isEmpty()) {
            log.info("没有找到任何公式");
            return List.of();
        }

        return formulaList.stream()
                .map(formula -> {
                    FormulaVO formulaVO = new FormulaVO();
                    formulaVO.setId(formula.getId());
                    formulaVO.setFormulaName(formula.getName());
                    formulaVO.setExpression(formula.getExpression());
                    formulaVO.setQuantities(formula.getQuantities());
                    formulaVO.setConstants(formula.getConstants());
                    return formulaVO;
                })
                .collect(Collectors.toList());

    }
}