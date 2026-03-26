package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.esports.zds.entity.TeamMember;

/**
 * 战队成员数据访问接口
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    /**
     * 根据战队ID查询成员列表
     */
    List<TeamMember> findByTeamId(Long teamId);

    /**
     * 根据用户ID查询其加入的战队关系
     */
    List<TeamMember> findByUserId(Long userId);

    /**
     * 检查用户是否已在战队中
     */
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    /**
     * 根据战队ID删除所有成员关系
     */
    @Modifying
    @Transactional
    void deleteByTeamId(Long teamId);
}
