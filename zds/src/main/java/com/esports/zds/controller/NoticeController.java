package com.esports.zds.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.entity.Notice;
import com.esports.zds.service.NoticeService;

/**
 * 公告模块控制器
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 获取公告列表
     */
    @GetMapping("/list")
    public Result<List<Notice>> list() {
        return Result.success(noticeService.listActiveNotices());
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Result<Notice> getInfo(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    /**
     * [管理端] 保存/更新公告
     */
    @PostMapping("/admin/save")
    public Result<String> save(@RequestBody Notice notice) {
        noticeService.save(notice);
        return Result.success("保存成功", null);
    }

    /**
     * [管理端] 删除公告
     */
    @PostMapping("/admin/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success("删除成功", null);
    }
}
