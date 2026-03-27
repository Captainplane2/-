package com.esports.zds.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esports.zds.entity.Team;
import com.esports.zds.entity.TeamMember;
import com.esports.zds.entity.TeamApplication;
import com.esports.zds.repository.TeamMemberRepository;
import com.esports.zds.repository.TeamRepository;
import com.esports.zds.repository.TeamApplicationRepository;
import com.esports.zds.repository.UserRepository;
import com.esports.zds.entity.User;
import com.esports.zds.service.TeamService;

/**
 * 战队业务实现类
 */
@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Team createTeam(Team team) {
        // 1. 规则校验：名称不超过10个字
        if (team.getName() == null || team.getName().trim().length() > 10) {
            throw new RuntimeException("战队名称不能超过10个字");
        }

        // 2. 规则校验：一个项目下只能创建一个战队
        List<TeamMember> myRelations = teamMemberRepository.findByUserId(team.getLeaderId());
        for (TeamMember rel : myRelations) {
            Team existingTeam = teamRepository.findById(rel.getTeamId()).orElse(null);
            if (existingTeam != null && existingTeam.getStatus() == 0 
                && existingTeam.getGameProject().equals(team.getGameProject())) {
                throw new RuntimeException("你在 " + team.getGameProject() + " 项目下已经加入了战队，不能再创建该项目的战队");
            }
        }

        // 3. 保存战队信息 (强制设为正常状态)
        team.setStatus(0);
        Team savedTeam = teamRepository.save(team);

        // 4. 将创建者设为队长
        TeamMember member = new TeamMember();
        member.setTeamId(savedTeam.getId());
        member.setUserId(team.getLeaderId());
        member.setRole(1); // 1 为队长
        teamMemberRepository.save(member);

        // 5. 自动升级身份为 ROLE_LEADER
        User user = userRepository.findById(team.getLeaderId()).orElseThrow();
        if (!"ROLE_ADMIN".equals(user.getRole())) {
            user.setRole("ROLE_LEADER");
            userRepository.save(user);
        }

        return savedTeam;
    }

    @Override
    public List<Team> listTeams(String university, String gameProject) {
        // 修复点：仅显示正常状态(status=0)的战队
        List<Team> all;
        if (university != null && !university.isEmpty()) {
            all = teamRepository.findByUniversity(university);
        } else if (gameProject != null && !gameProject.isEmpty()) {
            all = teamRepository.findByGameProject(gameProject);
        } else {
            all = teamRepository.findAll();
        }
        return all.stream().filter(t -> t.getStatus() == 0).toList();
    }

    @Override
    public Team getById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    @Override
    public void joinTeam(Long teamId, Long userId) {
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("你已经是该战队成员了");
        }

        Team targetTeam = teamRepository.findById(teamId).orElseThrow();
        if (targetTeam.getStatus() != 0) throw new RuntimeException("该战队已解散");

        // 检查同一项目是否已有战队
        List<TeamMember> myRelations = teamMemberRepository.findByUserId(userId);
        for (TeamMember rel : myRelations) {
            Team myTeam = teamRepository.findById(rel.getTeamId()).orElse(null);
            if (myTeam != null && myTeam.getStatus() == 0 && myTeam.getGameProject().equals(targetTeam.getGameProject())) {
                throw new RuntimeException("你在 " + targetTeam.getGameProject() + " 项目下已经有战队了");
            }
        }

        if (applicationRepository.existsByTeamIdAndUserIdAndStatus(teamId, userId, 0)) {
            throw new RuntimeException("申请已提交，请等待处理");
        }

        TeamApplication app = new TeamApplication();
        app.setTeamId(teamId);
        app.setUserId(userId);
        app.setStatus(0);
        applicationRepository.save(app);
    }

    @Override
    @Transactional
    public void reviewApplication(Long applicationId, Integer status) {
        TeamApplication app = applicationRepository.findById(applicationId).orElseThrow();
        if (app.getStatus() != 0) throw new RuntimeException("申请已处理");

        app.setStatus(status);
        applicationRepository.save(app);

        if (status == 1) {
            Team team = teamRepository.findById(app.getTeamId()).orElseThrow();
            
            // CS2战队最多5人
            if ("CS2".equals(team.getGameProject()) && team.getMemberCount() >= 5) {
                throw new RuntimeException("CS2战队最多5人，该战队已满员");
            }

            TeamMember member = new TeamMember();
            member.setTeamId(app.getTeamId());
            member.setUserId(app.getUserId());
            member.setRole(0);
            teamMemberRepository.save(member);

            team.setMemberCount(team.getMemberCount() + 1);
            teamRepository.save(team);

            // 更新用户角色为战队成员
            User user = userRepository.findById(app.getUserId()).orElseThrow();
            if (!"ROLE_ADMIN".equals(user.getRole()) && !"ROLE_LEADER".equals(user.getRole())) {
                user.setRole("ROLE_TEAM_MEMBER");
                userRepository.save(user);
            }
        }
    }

    @Override
    public List<TeamApplication> listPendingApplications(Long teamId) {
        return applicationRepository.findByTeamIdOrderByCreateTimeDesc(teamId);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        TeamMember target = members.stream()
                .filter(m -> m.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("该用户不是战队成员"));
        
        if (target.getRole() == 1) throw new RuntimeException("不能移除队长");

        teamMemberRepository.delete(target);

        Team team = teamRepository.findById(teamId).orElseThrow();
        team.setMemberCount(Math.max(1, team.getMemberCount() - 1));
        teamRepository.save(team);

        // 检查用户是否还有其他战队成员身份
        List<TeamMember> remainingMemberships = teamMemberRepository.findByUserId(userId);
        if (remainingMemberships.isEmpty()) {
            // 如果没有其他战队，更新用户角色为普通用户
            User user = userRepository.findById(userId).orElseThrow();
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                user.setRole("ROLE_USER");
                userRepository.save(user);
            }
        }
    }

    @Override
    public List<Team> listMyTeams(Long userId) {
        List<TeamMember> relations = teamMemberRepository.findByUserId(userId);
        List<Team> teams = new ArrayList<>();
        for (TeamMember rel : relations) {
            teamRepository.findById(rel.getTeamId()).ifPresent(t -> {
                if (t.getStatus() == 0) teams.add(t);
            });
        }
        return teams;
    }
}
