package com.bjfu.knowledge_graph.service;

public interface PhysicsSymbolMappingService {

    /**
     * 将物理公式中的特殊符号转换为标准数学符号
     */
    public String normalizePhysicsFormula(String formula);

    /**
     * 检查公式是否包含需要特殊处理的物理符号
     */
    public boolean containsPhysicsSymbols(String formula);
}
