package com.bjfu.knowledge_graph.controller;

import com.bjfu.knowledge_graph.bean.dto.SceneDTO;
import com.bjfu.knowledge_graph.bean.nodes.layer2.Scenario;
import com.bjfu.knowledge_graph.service.SceneService;
import com.bjfu.knowledge_graph.utils.ReturnMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scene")
public class SceneController {
    @Autowired
    private SceneService sceneService;

    @PostMapping("/add/scene")
    public ReturnMsg addScene(@RequestBody SceneDTO sceneDTO) {
        Scenario scenario = sceneService.addScene(sceneDTO);
        if (scenario != null) {
            return ReturnMsg.success(ReturnMsg.SUCCESS,"场景添加成功");
        } else {
            return ReturnMsg.fail(ReturnMsg.FAIL,"场景添加失败，请检查输入数据");
        }
    }

}
