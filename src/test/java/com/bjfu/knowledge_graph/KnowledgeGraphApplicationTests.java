package com.bjfu.knowledge_graph;


import com.bjfu.knowledge_graph.service.KnowledgeGraphService;
import com.bjfu.knowledge_graph.service.impl.KnowledgeGraphIngestionServiceImpl;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;


@SpringBootTest
class KnowledgeGraphApplicationTests {
    @Autowired
    private KnowledgeGraphIngestionServiceImpl ingestionService;
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Test
    void contextLoads() {
        ingestionService.ingestKnowledgeGraph();

    }

    @Test
    void test(){
        knowledgeGraphService.initFormulas();
    }

}
