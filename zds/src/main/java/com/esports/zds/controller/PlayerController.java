package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.dto.PlayerDTO;
import com.esports.zds.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选手信息控制器
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    /**
     * 获取选手列表
     * @param page 页码
     * @param size 每页大小
     * @param gameProject 游戏项目（可选）
     * @param keyword 关键词搜索（可选）
     * @param university 高校筛选（可选）
     * @param teamId 战队ID筛选（可选）
     * @param role 身份筛选（可选）
     * @return 选手列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getPlayerList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String gameProject,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String role) {
        
        Map<String, Object> result = playerService.getPlayerList(
                page, size, gameProject, keyword, university, teamId, role);
        
        return Result.success("获取选手列表成功", result);
    }

    /**
     * 获取指定游戏板块的选手列表
     * @param gameProject 游戏项目
     * @param page 页码
     * @param size 每页大小
     * @return 选手列表
     */
    @GetMapping("/list/{gameProject}")
    public Result<Map<String, Object>> getPlayersByGameProject(
            @PathVariable String gameProject,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> result = playerService.getPlayerList(
                page, size, gameProject, null, null, null, null);
        
        return Result.success("获取选手列表成功", result);
    }
}
