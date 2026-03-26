package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.MatchApplication;

/**
 * 约赛申请数据访问接口
 */
@Repository
public interface MatchApplicationRepository extends JpaRepository<MatchApplication, Long> {
    
    /**
     * 根据约赛ID查询申请列表
     */
    List<MatchApplication> findByMatchId(Long matchId);

    /**
     * 检查某战队是否已申请过该约赛
     */
    boolean existsByMatchIdAndApplicantTeamId(Long matchId, Long applicantTeamId);
}
