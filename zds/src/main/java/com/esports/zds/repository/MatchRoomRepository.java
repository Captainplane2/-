package com.esports.zds.repository;

import com.esports.zds.entity.MatchRoom;
import com.esports.zds.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRoomRepository extends JpaRepository<MatchRoom, Long> {
    // 获取指定状态的约战房间列表
    List<MatchRoom> findByStatusOrderByCreateTimeDesc(Integer status);
    
    // 获取所有约战房间列表并按时间倒序
    List<MatchRoom> findAllByOrderByCreateTimeDesc();
    
    // 按项目和状态筛选
    List<MatchRoom> findByGameProjectAndStatusOrderByCreateTimeDesc(String gameProject, Integer status);
    
    // 获取所有未结束的约战（状态为0招募中或1已应战）
    List<MatchRoom> findByStatusInOrderByCreateTimeDesc(List<Integer> statuses);
    
    // 按项目获取所有未结束的约战
    List<MatchRoom> findByGameProjectAndStatusInOrderByCreateTimeDesc(String gameProject, List<Integer> statuses);
    
    // === 新增状态管理相关查询方法 ===
    
    // 根据新状态枚举查询
    List<MatchRoom> findByMatchStatusOrderByCreateTimeDesc(MatchStatus matchStatus);
    
    // 根据多个新状态枚举查询
    List<MatchRoom> findByMatchStatusInOrderByCreateTimeDesc(List<MatchStatus> matchStatuses);
    
    // 查询需要检查过期状态的比赛
    @Query("SELECT m FROM MatchRoom m WHERE m.matchStatus IN :statuses AND m.matchTime IS NOT NULL")
    List<MatchRoom> findByMatchStatusIn(@Param("statuses") List<MatchStatus> statuses);
    
    // 查询正在进行倒计时的比赛
    @Query("SELECT m FROM MatchRoom m WHERE m.countdownStartTime IS NOT NULL AND m.countdownSeconds IS NOT NULL")
    List<MatchRoom> findMatchesWithActiveCountdown();
}
