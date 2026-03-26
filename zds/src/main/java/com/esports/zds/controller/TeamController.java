package com.esports.zds.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.entity.Team;
import com.esports.zds.entity.TeamApplication;
import com.esports.zds.entity.TeamMember;
import com.esports.zds.repository.TeamRepository;
import com.esports.zds.repository.TeamMemberRepository;
import com.esports.zds.repository.UserRepository;
import com.esports.zds.service.TeamService;

/**
 * 战队模块控制器
 */
@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 创建战队
     */
    @PostMapping("/create")
    public Result<Team> create(@RequestBody Team team) {
        Team savedTeam = teamService.createTeam(team);
        return Result.success("战队创建成功", savedTeam);
    }

    /**
     * 获取战队列表
     */
    @GetMapping("/list")
    public Result<List<Team>> list(
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String gameProject) {
        List<Team> teams = teamService.listTeams(university, gameProject);
        return Result.success(teams);
    }

    /**
     * 获取战队详情
     */
    @GetMapping("/{id}")
    public Result<Team> getInfo(@PathVariable Long id) {
        Team team = teamService.getById(id);
        return Result.success(team);
    }

    /**
     * 获取战队成员列表 (带用户信息)
     */
    @GetMapping("/members/{teamId}")
    public Result<List<Map<String, Object>>> listMembers(@PathVariable Long teamId) {
        List<TeamMember> relations = teamMemberRepository.findByTeamId(teamId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamMember rel : relations) {
            Map<String, Object> map = new HashMap<>();
            userRepository.findById(rel.getUserId()).ifPresent(user -> {
                map.put("id", user.getId());
                map.put("nickname", user.getNickname());
                map.put("username", user.getUsername());
                map.put("role", rel.getRole()); // 队内角色
                map.put("joinTime", rel.getJoinTime());
            });
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 申请加入战队
     */
    @PostMapping("/join/{teamId}")
    public Result<String> join(@PathVariable Long teamId, @RequestParam Long userId) {
        teamService.joinTeam(teamId, userId);
        return Result.success("申请加入成功", null);
    }

    /**
     * 获取我的战队
     */
    @GetMapping("/my/{userId}")
    public Result<List<Team>> listMyTeams(@PathVariable Long userId) {
        List<Team> teams = teamService.listMyTeams(userId);
        return Result.success(teams);
    }

    /**
     * 获取战队申请列表 (队长调用)
     */
    @GetMapping("/applications/{teamId}")
    public Result<List<TeamApplication>> listApplications(@PathVariable Long teamId) {
        return Result.success(teamService.listPendingApplications(teamId));
    }

    /**
     * 审核入队申请
     */
    @PostMapping("/applications/review/{appId}")
    public Result<String> reviewApplication(@PathVariable Long appId, @RequestParam Integer status) {
        teamService.reviewApplication(appId, status);
        return Result.success("审核操作已完成", null);
    }

    /**
     * 移除战队成员
     */
    @PostMapping("/member/remove")
    public Result<String> removeMember(@RequestParam Long teamId, @RequestParam Long userId) {
        teamService.removeMember(teamId, userId);
        return Result.success("成员已移除", null);
    }

    /**
     * [管理端] 获取所有战队列表
     */
    @GetMapping("/admin/list")
    public Result<List<Team>> listAll() {
        return Result.success(teamRepository.findAll());
    }

    /**
     * [管理端] 强制解散战队 (逻辑删除)
     */
    @PostMapping("/admin/delete/{id}")
    public Result<String> deleteTeam(@PathVariable Long id) {
        Team team = teamRepository.findById(id).orElse(null);
        if (team != null) {
            team.setStatus(1); // 1 表示解散状态
            teamRepository.save(team);
        }
        return Result.success("战队已成功解散并从前台移除", null);
    }

    /**
     * [管理端] 彻底删除战队记录 (物理删除)
     */
    @PostMapping("/admin/real-delete/{id}")
    public Result<String> realDeleteTeam(@PathVariable Long id) {
        teamRepository.deleteById(id);
        // 同步删除成员关系
        teamMemberRepository.deleteByTeamId(id);
        return Result.success("战队记录已彻底从数据库删除", null);
    }
}
