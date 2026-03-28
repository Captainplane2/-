package com.esports.zds.service;

import com.esports.zds.dto.PlayerDTO;
import com.esports.zds.entity.Team;
import com.esports.zds.entity.TeamMember;
import com.esports.zds.entity.User;
import com.esports.zds.repository.TeamMemberRepository;
import com.esports.zds.repository.TeamRepository;
import com.esports.zds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 选手信息服务
 */
@Service
public class PlayerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * 获取选手列表
     * @param page 页码
     * @param size 每页大小
     * @param gameProject 游戏项目
     * @param keyword 关键词
     * @param university 高校
     * @param teamId 战队ID
     * @param role 身份
     * @return 选手列表和分页信息
     */
    public Map<String, Object> getPlayerList(int page, int size, String gameProject, 
                                             String keyword, String university, 
                                             Long teamId, String role) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有已加入战队的用户ID（去重）
        List<TeamMember> allMembers = teamMemberRepository.findAll();
        
        // 如果指定了游戏项目，筛选该项目的成员
        final String finalGameProject = gameProject;
        if (finalGameProject != null && !finalGameProject.isEmpty()) {
            allMembers = allMembers.stream()
                    .filter(m -> finalGameProject.equalsIgnoreCase(m.getGameProject()))
                    .collect(Collectors.toList());
        }
        
        // 如果指定了战队ID，筛选该战队的成员
        final Long finalTeamId = teamId;
        if (finalTeamId != null) {
            allMembers = allMembers.stream()
                    .filter(m -> finalTeamId.equals(m.getTeamId()))
                    .collect(Collectors.toList());
        }
        
        // 获取用户ID集合（去重）
        Set<Long> userIds = allMembers.stream()
                .map(TeamMember::getUserId)
                .collect(Collectors.toSet());
        
        // 如果没有符合条件的用户，返回空结果
        if (userIds.isEmpty()) {
            result.put("records", new ArrayList<>());
            result.put("total", 0);
            result.put("pages", 0);
            result.put("current", page);
            result.put("size", size);
            return result;
        }
        
        // 分页查询用户
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage;
        
        // 构建查询条件
        final String finalKeyword = keyword;
        final String finalUniversity = university;
        if (finalKeyword != null && !finalKeyword.isEmpty()) {
            // 关键词搜索
            userPage = userRepository.findByIdInAndNicknameContainingOrUsernameContaining(
                    userIds, finalKeyword, finalKeyword, pageable);
        } else if (finalUniversity != null && !finalUniversity.isEmpty()) {
            // 高校筛选
            userPage = userRepository.findByIdInAndUniversity(userIds, finalUniversity, pageable);
        } else {
            // 基础查询
            userPage = userRepository.findByIdIn(userIds, pageable);
        }
        
        // 转换为DTO
        final List<TeamMember> finalAllMembers = allMembers;
        List<PlayerDTO> playerList = userPage.getContent().stream()
                .map(user -> convertToDTO(user, finalAllMembers, finalGameProject))
                .collect(Collectors.toList());
        
        // 如果指定了角色，进行过滤
        final String finalRole = role;
        if (finalRole != null && !finalRole.isEmpty()) {
            playerList = playerList.stream()
                    .filter(p -> finalRole.equals(p.getRole()))
                    .collect(Collectors.toList());
        }
        
        result.put("records", playerList);
        result.put("total", userPage.getTotalElements());
        result.put("pages", userPage.getTotalPages());
        result.put("current", page);
        result.put("size", size);
        
        return result;
    }

    /**
     * 将User转换为PlayerDTO
     * @param user 用户实体
     * @param allMembers 所有战队成员关系
     * @param gameProject 游戏项目（可选）
     * @return PlayerDTO
     */
    private PlayerDTO convertToDTO(User user, List<TeamMember> allMembers, String gameProject) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setUniversity(user.getUniversity());
        dto.setCreateTime(user.getCreateTime());
        
        // 获取用户的战队成员关系
        List<TeamMember> userMembers = allMembers.stream()
                .filter(m -> m.getUserId().equals(user.getId()))
                .collect(Collectors.toList());
        
        // 如果指定了游戏项目，只保留该项目的成员关系
        if (gameProject != null && !gameProject.isEmpty()) {
            userMembers = userMembers.stream()
                    .filter(m -> gameProject.equalsIgnoreCase(m.getGameProject()))
                    .collect(Collectors.toList());
        }
        
        // 构建战队信息列表
        List<PlayerDTO.TeamInfo> teamInfos = new ArrayList<>();
        Set<String> gameProjects = new HashSet<>();
        
        for (TeamMember member : userMembers) {
            // 获取战队信息
            Optional<Team> teamOpt = teamRepository.findById(member.getTeamId());
            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                PlayerDTO.TeamInfo teamInfo = new PlayerDTO.TeamInfo();
                teamInfo.setId(team.getId());
                teamInfo.setName(team.getName());
                teamInfo.setRole(convertRole(member.getRole()));
                teamInfo.setGameProject(member.getGameProject());
                teamInfos.add(teamInfo);
                
                // 收集游戏项目
                if (member.getGameProject() != null) {
                    gameProjects.add(member.getGameProject());
                }
            }
        }
        
        dto.setTeams(teamInfos);
        dto.setGameProjects(new ArrayList<>(gameProjects));
        
        // 设置用户角色（取最高权限）
        String userRole = determineUserRole(userMembers);
        dto.setRole(userRole);
        
        return dto;
    }

    /**
     * 转换角色代码为角色字符串
     * @param role 角色代码
     * @return 角色字符串
     */
    private String convertRole(Integer role) {
        if (role == null) return "ROLE_USER";
        switch (role) {
            case 1: return "ROLE_LEADER";
            case 0: return "ROLE_TEAM_MEMBER";
            default: return "ROLE_USER";
        }
    }

    /**
     * 确定用户角色（取最高权限）
     * @param members 战队成员列表
     * @return 用户角色
     */
    private String determineUserRole(List<TeamMember> members) {
        if (members == null || members.isEmpty()) {
            return "ROLE_USER";
        }
        
        // 检查是否有队长角色
        boolean hasLeader = members.stream()
                .anyMatch(m -> m.getRole() != null && m.getRole() == 1);
        
        if (hasLeader) {
            return "ROLE_LEADER";
        }
        
        // 检查是否有队员角色
        boolean hasMember = members.stream()
                .anyMatch(m -> m.getRole() != null && m.getRole() == 0);
        
        if (hasMember) {
            return "ROLE_TEAM_MEMBER";
        }
        
        return "ROLE_USER";
    }
}
