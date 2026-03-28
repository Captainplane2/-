package com.esports.zds.repository;

import com.esports.zds.entity.MatchStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchStatusHistoryRepository extends JpaRepository<MatchStatusHistory, Long> {
    
    /**
     * 根据约战ID查询状态变更历史
     */
    @Query("SELECT h FROM MatchStatusHistory h WHERE h.matchRoom.id = :matchId ORDER BY h.changeTime DESC")
    List<MatchStatusHistory> findByMatchRoomId(@Param("matchId") Long matchId);
    
    /**
     * 根据约战ID和状态查询最新的状态变更记录
     */
    @Query("SELECT h FROM MatchStatusHistory h WHERE h.matchRoom.id = :matchId AND h.toStatus = :status ORDER BY h.changeTime DESC LIMIT 1")
    MatchStatusHistory findLatestByMatchIdAndStatus(@Param("matchId") Long matchId, @Param("status") String status);
}