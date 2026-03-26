package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.TeamApplication;

/**
 * 战队申请数据访问接口
 */
@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    
    /**
     * 根据战队ID查询所有申请
     */
    List<TeamApplication> findByTeamIdOrderByCreateTimeDesc(Long teamId);

    /**
     * 检查某用户是否已申请过该战队且在处理中
     */
    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, Long userId, Integer status);
}
