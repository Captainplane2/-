package com.esports.zds.repository;

import com.esports.zds.entity.MatchRoom;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
