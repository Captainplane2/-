package com.esports.zds.service;

import com.esports.zds.entity.Team;
import com.esports.zds.entity.TeamMember;
import com.esports.zds.entity.User;
import com.esports.zds.repository.TeamMemberRepository;
import com.esports.zds.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserIdentityService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * 获取用户在指定游戏板块的身份
     * @param userId 用户ID
     * @param gameProject 游戏项目
     * @return 身份标签，如果没有则返回null
     */
    public String getUserIdentityInGameProject(Long userId, String gameProject) {
        // 查找用户在该游戏板块的所有战队成员关系
        List<TeamMember> teamMembers = teamMemberRepository.findByUserIdAndGameProject(userId, gameProject);
        
        // 按角色优先级排序（队长 > 成员）
        teamMembers.sort((a, b) -> b.getRole().compareTo(a.getRole()));
        
        if (!teamMembers.isEmpty()) {
            TeamMember member = teamMembers.get(0);
            if (member.getRole() == 1) {
                return "战队队长";
            } else if (member.getRole() == 0) {
                return "战队成员";
            }
        }
        
        return null;
    }

    /**
     * 获取用户在所有游戏板块的身份
     * @param userId 用户ID
     * @return 按游戏板块分组的身份标签
     */
    public Map<String, String> getUserIdentitiesAcrossGameProjects(Long userId) {
        Map<String, String> identities = new HashMap<>();
        
        // 查找用户的所有战队成员关系
        List<TeamMember> teamMembers = teamMemberRepository.findByUserId(userId);
        
        // 按游戏板块分组，过滤掉 gameProject 为 null 的记录
        Map<String, List<TeamMember>> membersByGameProject = teamMembers.stream()
                .filter(member -> member.getGameProject() != null)
                .collect(Collectors.groupingBy(TeamMember::getGameProject));
        
        // 为每个游戏板块选择最高角色
        for (Map.Entry<String, List<TeamMember>> entry : membersByGameProject.entrySet()) {
            String gameProject = entry.getKey();
            List<TeamMember> members = entry.getValue();
            
            // 按角色优先级排序
            members.sort((a, b) -> b.getRole().compareTo(a.getRole()));
            
            if (!members.isEmpty()) {
                TeamMember member = members.get(0);
                if (member.getRole() == 1) {
                    identities.put(gameProject, "战队队长");
                } else if (member.getRole() == 0) {
                    identities.put(gameProject, "战队成员");
                }
            }
        }
        
        return identities;
    }

    /**
     * 更新用户在游戏板块的身份
     * @param teamId 战队ID
     * @param userId 用户ID
     * @param role 角色 (0: 成员, 1: 队长)
     */
    public void updateUserIdentity(Long teamId, Long userId, Integer role) {
        // 查找战队信息
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return;
        }
        
        // 查找或创建战队成员关系
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElse(new TeamMember());
        
        teamMember.setTeamId(teamId);
        teamMember.setUserId(userId);
        teamMember.setRole(role);
        teamMember.setGameProject(team.getGameProject());
        
        teamMemberRepository.save(teamMember);
    }

    /**
     * 删除用户在游戏板块的身份
     * @param teamId 战队ID
     * @param userId 用户ID
     */
    public void removeUserIdentity(Long teamId, Long userId) {
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }
}
