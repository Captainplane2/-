package com.esports.zds.service;

import java.util.List;

import com.esports.zds.entity.Team;
import com.esports.zds.entity.TeamApplication;

/**
 * 战队业务接口
 */
public interface TeamService {
    
    /**
     * 创建战队
     */
    Team createTeam(Team team);

    /**
     * 获取战队列表
     */
    List<Team> listTeams(String university, String gameProject);

    /**
     * 获取战队详情
     */
    Team getById(Long id);

    /**
     * 申请加入战队
     */
    void joinTeam(Long teamId, Long userId);

    /**
     * 获取用户加入的战队列表
     */
    List<Team> listMyTeams(Long userId);

    /**
     * 审核入队申请
     */
    void reviewApplication(Long applicationId, Integer status);

    /**
     * 获取某战队的待处理申请
     */
    List<TeamApplication> listPendingApplications(Long teamId);

    /**
     * 从战队中移除成员
     */
    void removeMember(Long teamId, Long userId);
}
