package com.bjfu.knowledge_graph.controller;

import com.bjfu.knowledge_graph.bean.dto.FormulaDTO;
import com.bjfu.knowledge_graph.bean.nodes.layer1.Formula;
import com.bjfu.knowledge_graph.service.KnowledgeGraphService;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import com.bjfu.knowledge_graph.bean.vo.FormulaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class FormulaController {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    // 获取全部公式
    @GetMapping("/getAll")
    public ReturnMsg<List<FormulaVO>> getAllFormulas() {
        List<FormulaVO> allFormulas = knowledgeGraphService.getAllFormulas();
        return ReturnMsg.success(ReturnMsg.SUCCESS, "获取全部公式成功",allFormulas);
    }

    @GetMapping("/getFormulaById/{id}")
    public ReturnMsg<FormulaVO> getFormulaByName(@PathVariable("id") Long id) {
        FormulaVO formulaVO = knowledgeGraphService.getFormulaById(id);
        if (formulaVO != null) {
            return ReturnMsg.success(ReturnMsg.SUCCESS, "获取公式成功", formulaVO);
        } else {
            return ReturnMsg.fail(ReturnMsg.ERROR, "公式不存在");
        }
    }
    /**
     * 添加公式
     * @param formulaDTO
     * @return
     */
    @PostMapping("/addFormula")
    public ReturnMsg addFormula(@RequestBody FormulaDTO formulaDTO) {
        Formula formula = knowledgeGraphService.addFormula(formulaDTO);
        if (formula != null) {
            log.info("添加公式成功: {}", formula.getName());
            return ReturnMsg.success(ReturnMsg.SUCCESS, "添加公式成功");
        } else {
            log.error("添加公式失败: {}", formulaDTO.getFormula());
            return ReturnMsg.fail(ReturnMsg.ERROR, "添加公式失败");
        }
    }
    // 更新公式
    @PutMapping("/updateFormula")
    public ReturnMsg updateFormula() {
        // 这里应该调用服务层方法更新公式
        return ReturnMsg.success(ReturnMsg.SUCCESS, "更新公式成功");
    }

    // 删除公式
    @DeleteMapping("/deleteFormula/{id}")
    public ReturnMsg deleteFormula(@PathVariable Long id) {
        return knowledgeGraphService.deleteFormula(id);
    }

}
