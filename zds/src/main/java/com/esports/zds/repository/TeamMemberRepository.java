package com.esports.zds.repository;

import com.esports.zds.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * 根据用户ID和游戏项目查询战队成员关系
     */
    List<TeamMember> findByUserIdAndGameProject(Long userId, String gameProject);

    /**
     * 根据用户ID查询所有战队成员关系
     */
    List<TeamMember> findByUserId(Long userId);

    /**
     * 根据战队ID和用户ID查询战队成员关系
     */
    Optional<TeamMember> findByTeamIdAndUserId(Long teamId, Long userId);

    /**
     * 根据战队ID查询所有战队成员关系
     */
    List<TeamMember> findByTeamId(Long teamId);

    /**
     * 检查战队ID和用户ID是否存在
     */
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    /**
     * 根据战队ID和用户ID删除战队成员关系
     */
    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.teamId = :teamId AND tm.userId = :userId")
    void deleteByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    /**
     * 根据战队ID删除所有战队成员关系
     */
    @Modifying
    @Query("DELETE FROM TeamMember tm WHERE tm.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
