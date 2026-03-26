package com.esports.zds.service;

import java.util.List;

import com.esports.zds.entity.MatchApplication;
import com.esports.zds.entity.MatchInfo;

/**
 * 约赛业务接口
 */
public interface MatchService {
    
    /**
     * 发布约赛
     */
    MatchInfo publishMatch(MatchInfo matchInfo);

    /**
     * 获取约赛列表 (带筛选)
     */
    List<MatchInfo> listMatches(Integer status, String gameProject);

    /**
     * 获取约赛详情
     */
    MatchInfo getById(Long id);

    /**
     * 申请对接约赛
     */
    void applyMatch(Long matchId, Long applicantTeamId);

    /**
     * 审核约赛申请
     */
    void reviewApplication(Long applicationId, Integer status);

    /**
     * 获取某约赛的申请列表
     */
    List<MatchApplication> listApplications(Long matchId);

    /**
     * 获取我的战队约赛记录
     */
    List<MatchInfo> listMyMatches(Long teamId);
}
