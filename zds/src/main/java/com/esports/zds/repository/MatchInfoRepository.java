package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.MatchInfo;

/**
 * 约赛数据访问接口
 */
@Repository
public interface MatchInfoRepository extends JpaRepository<MatchInfo, Long> {
    
    /**
     * 根据状态查询约赛列表
     */
    List<MatchInfo> findByStatus(Integer status);

    /**
     * 根据游戏项目查询约赛列表
     */
    List<MatchInfo> findByGameProject(String gameProject);
}
