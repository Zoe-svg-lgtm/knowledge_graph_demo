package com.bjfu.knowledge_graph.service.impl;

import com.bjfu.knowledge_graph.service.PhysicsSymbolMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class PhysicsSymbolMappingServiceImpl implements PhysicsSymbolMappingService {
    // 物理符号到标准符号的映射
    private static final Map<String, String> PHYSICS_SYMBOL_MAPPING = new HashMap<>();

    static {
        // 力相关符号
        PHYSICS_SYMBOL_MAPPING.put("ΣF", "F");      // 合力
        PHYSICS_SYMBOL_MAPPING.put("∑F", "F");      // 合力（另一种写法）
        PHYSICS_SYMBOL_MAPPING.put("F合", "F");     // 中文合力
        PHYSICS_SYMBOL_MAPPING.put("F_合", "F");    // 下标形式
        PHYSICS_SYMBOL_MAPPING.put("F_net", "F");  // 英文net force
        PHYSICS_SYMBOL_MAPPING.put("Fnet", "F");   // 英文net force简写

        // 其他可能的物理符号
        PHYSICS_SYMBOL_MAPPING.put("Σv", "v");     // 合速度
        PHYSICS_SYMBOL_MAPPING.put("Σa", "a");     // 合加速度
        PHYSICS_SYMBOL_MAPPING.put("ΣE", "E");     // 总能量

        // 角标和特殊符号
        PHYSICS_SYMBOL_MAPPING.put("v₀", "v0");    // 初速度
        PHYSICS_SYMBOL_MAPPING.put("v₁", "v1");    // 末速度
        PHYSICS_SYMBOL_MAPPING.put("F₁", "F1");    // 力1
        PHYSICS_SYMBOL_MAPPING.put("F₂", "F2");    // 力2

        log.info("Physics symbol mapping initialized with {} mappings", PHYSICS_SYMBOL_MAPPING.size());
    }


    @Override
    public String normalizePhysicsFormula(String formula) {
        if (formula == null || formula.isEmpty()) {
            return formula;
        }

        String normalized = formula;
        log.debug("Original formula: {}", formula);

        // 替换所有映射的符号
        for (Map.Entry<String, String> entry : PHYSICS_SYMBOL_MAPPING.entrySet()) {
            String physicsSymbol = entry.getKey();
            String standardSymbol = entry.getValue();

            if (normalized.contains(physicsSymbol)) {
                normalized = normalized.replace(physicsSymbol, standardSymbol);
                log.debug("Replaced '{}' with '{}': {}", physicsSymbol, standardSymbol, normalized);
            }
        }

        // 处理其他特殊情况
        normalized = handleSpecialCases(normalized);

        log.info("Formula normalization: '{}' -> '{}'", formula, normalized);
        return normalized;
    }

    /**
     * 处理特殊情况
     */
    private String handleSpecialCases(String formula) {
        String processed = formula;

        // 处理求和符号后的表达式，如 ΣF = F1 + F2 + F3
        if (formula.contains("Σ") || formula.contains("∑")) {
            // 这种情况下，我们假设求和结果就是合力
            processed = processed.replaceAll("[Σ∑]([A-Za-z]+)", "$1");
        }

        // 处理下标符号
        processed = processed.replaceAll("([A-Za-z]+)_([A-Za-z0-9]+)", "$1$2");

        // 处理上标符号（除了幂次）
        processed = processed.replaceAll("([A-Za-z]+)\\^([A-Za-z]+)", "$1$2");

        return processed;
    }

    @Override
    public boolean containsPhysicsSymbols(String formula) {
        if (formula == null) return false;

        for (String symbol : PHYSICS_SYMBOL_MAPPING.keySet()) {
            if (formula.contains(symbol)) {
                return true;
            }
        }

        return formula.contains("Σ") || formula.contains("∑") ||
                formula.contains("_") || Pattern.compile("[A-Za-z]+[₀-₉]+").matcher(formula).find();
    }
}
