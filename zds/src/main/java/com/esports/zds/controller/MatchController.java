package com.esports.zds.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.entity.MatchApplication;
import com.esports.zds.entity.MatchInfo;
import com.esports.zds.repository.MatchInfoRepository;
import com.esports.zds.service.MatchService;

/**
 * 约赛模块控制器
 */
@RestController
@RequestMapping("/api/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchInfoRepository matchInfoRepository;

    /**
     * 发布约赛
     */
    @PostMapping("/publish")
    public Result<MatchInfo> publish(@RequestBody MatchInfo matchInfo) {
        MatchInfo savedMatch = matchService.publishMatch(matchInfo);
        return Result.success("发布成功", savedMatch);
    }

    /**
     * 获取约赛列表
     */
    @GetMapping("/list")
    public Result<List<MatchInfo>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String gameProject) {
        List<MatchInfo> matches = matchService.listMatches(status, gameProject);
        return Result.success(matches);
    }

    /**
     * 获取约赛详情
     */
    @GetMapping("/{id}")
    public Result<MatchInfo> getInfo(@PathVariable Long id) {
        MatchInfo match = matchService.getById(id);
        return Result.success(match);
    }

    /**
     * 申请对接约赛
     */
    @PostMapping("/apply/{matchId}")
    public Result<String> apply(@PathVariable Long matchId, @RequestParam Long teamId) {
        matchService.applyMatch(matchId, teamId);
        return Result.success("申请对接成功", null);
    }

    /**
     * 获取申请列表
     */
    @GetMapping("/applications/{matchId}")
    public Result<List<MatchApplication>> listApplications(@PathVariable Long matchId) {
        List<MatchApplication> apps = matchService.listApplications(matchId);
        return Result.success(apps);
    }

    /**
     * 审核申请
     */
    @PostMapping("/review/{applicationId}")
    public Result<String> review(@PathVariable Long applicationId, @RequestParam Integer status) {
        matchService.reviewApplication(applicationId, status);
        return Result.success("审核操作成功", null);
    }

    /**
     * 获取我的约赛记录
     */
    @GetMapping("/my/{teamId}")
    public Result<List<MatchInfo>> listMyMatches(@PathVariable Long teamId) {
        List<MatchInfo> matches = matchService.listMyMatches(teamId);
        return Result.success(matches);
    }

    /**
     * [管理端] 获取所有约赛列表
     */
    @GetMapping("/admin/list")
    public Result<List<MatchInfo>> listAll() {
        return Result.success(matchInfoRepository.findAll());
    }

    /**
     * [管理端] 更新约赛状态
     */
    @PostMapping("/admin/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        MatchInfo match = matchInfoRepository.findById(id).orElseThrow();
        match.setStatus(status);
        matchInfoRepository.save(match);
        return Result.success("状态更新成功", null);
    }
}
