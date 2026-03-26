package com.esports.zds.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.entity.Message;
import com.esports.zds.service.MessageService;

/**
 * 留言模块控制器
 */
@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 提交留言
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Message message) {
        messageService.submit(message);
        return Result.success("留言提交成功", null);
    }

    /**
     * 获取我的留言
     */
    @GetMapping("/my/{userId}")
    public Result<List<Message>> listMy(@PathVariable Long userId) {
        return Result.success(messageService.listByUserId(userId));
    }

    /**
     * 管理员回复留言
     */
    @PostMapping("/reply/{id}")
    public Result<String> reply(@PathVariable Long id, @RequestBody Map<String, String> data) {
        String content = data.get("content");
        messageService.reply(id, content);
        return Result.success("回复成功", null);
    }
}
