package com.bjfu.knowledge_graph.controller;

import com.bjfu.knowledge_graph.utils.ReturnMsg;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class KnowledgeController {

    @GetMapping("/getAllKnowledge")
    public ReturnMsg getAllKnowledge() {
        return ReturnMsg.success(ReturnMsg.SUCCESS, "成功", null);
    }
}
