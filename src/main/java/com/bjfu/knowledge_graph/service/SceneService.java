package com.bjfu.knowledge_graph.service;

import com.bjfu.knowledge_graph.bean.dto.SceneDTO;
import com.bjfu.knowledge_graph.bean.nodes.layer2.Scenario;
import com.bjfu.knowledge_graph.utils.ReturnMsg;

public interface SceneService {
    /**
     * 添加场景
     *
     * @param sceneDTO 场景数据传输对象
     * @return 返回操作结果
     */
    Scenario addScene(SceneDTO sceneDTO);
}
