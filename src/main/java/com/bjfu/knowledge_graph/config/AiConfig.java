package com.bjfu.knowledge_graph.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    public interface FormulaAnalysisService {

        @SystemMessage("""
        你是一个专业的高中物理公式分析专家。你需要分析用户输入的物理公式，并将其转换为结构化的知识图谱数据。
        
        请按照以下JSON格式返回分析结果：
        {
          "formulaName": "公式名称",
          "expression": "公式表达式",
          "conditions": "适用条件",
          "quantities": [
            {
              "name": "物理量名称",
              "symbol": "符号",
              "unit": "单位",
              "isVector": true/false,
              "description": "描述"
            }
          ],
          "constants": [
            {
              "name": "常数名称",
              "symbol": "符号",
              "value": 数值,
              "unit": "单位",
              "description": "描述"
            }
          ]
        }
        
        注意：
        1. 只返回JSON格式，不要包含其他文字
        2. 物理量要包含所有出现在公式中的变量
        3. 常数要包含所有物理常数
        4. 准确判断物理量是否为矢量
        5. 单位使用标准的SI单位
        """)
        @UserMessage("请分析以下高中物理公式：{{formula}}")
        String analyzeFormula(@V("formula") String formula);
    }
    @Bean
     public FormulaAnalysisService formulaAnalysisService(ChatLanguageModel qwenChatModel) {
        return AiServices.builder(FormulaAnalysisService.class)
                .chatLanguageModel(qwenChatModel)
                .build();
        }

    public interface ProblemParsingService {

        @SystemMessage("""
        你是一个物理问题分析助理。你的任务是从用户的自然语言问题中，抽取出所有已知的物理量（包括它们的数值和单位）以及用户想要求解的未知物理量。
        
        请严格按照以下JSON格式返回结果，不要包含任何多余的解释：
        {
          "knowns": [
            { "name": "物理量标准名称", "value": 数值 }
          ],
          "unknown": "待求物理量的标准名称"
        }
        
        例如，对于问题“一个10kg的物体，加速度是2m/s^2，请问它受到的力是多少？”，你应该返回：
        {
          "knowns": [
            { "name": "质量", "value": 10 },
            { "name": "加速度", "value": 2 }
          ],
          "unknown": "力"
        }
        """)
        @UserMessage("请分析这个问题：{{question}}")
        String parseProblem(@V("question") String question);
    }

    @Bean
    public ProblemParsingService problemParsingService(ChatLanguageModel qwenChatModel) {
        return AiServices.builder(ProblemParsingService.class)
                .chatLanguageModel(qwenChatModel)
                .build();
    }
}
