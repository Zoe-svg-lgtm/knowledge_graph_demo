package com.bjfu.knowledge_graph.bean.dto;

import lombok.Data;

import java.net.SocketOption;

@Data
public class FormulaDTO {
    private String formula;
    private String context; // 可选的上下文信息

}
