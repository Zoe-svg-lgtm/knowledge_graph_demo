package com.bjfu.knowledge_graph.service;

import com.bjfu.knowledge_graph.bean.dto.FormulaDTO;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import com.bjfu.knowledge_graph.bean.vo.FormulaVO;

import java.util.List;

public interface KnowledgeGraphService {
    /**
     * 添加公式
     * @param formulaDTO 公式数据传输对象
     * @return 添加结果
     */
    Formula addFormula(FormulaDTO formulaDTO);
    /**
     * 获取公式详情
     * @return 公式视图对象
     */
    List<FormulaVO> getAllFormulas();
    /**
     * 根据ID获取公式详情
     * @param formulaId 公式ID
     * @return 公式视图对象
     */
    public ReturnMsg deleteFormula(Long formulaId);

    /**
     * 根据公式名称获取公式详情
     * @param formulaId 公式id
     * @return 公式视图对象
     */
    FormulaVO getFormulaById(Long formulaId);

}