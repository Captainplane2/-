package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.Team;

/**
 * 战队数据访问接口
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    /**
     * 根据高校名称查询战队列表
     */
    List<Team> findByUniversity(String university);

    /**
     * 根据游戏项目查询战队列表
     */
    List<Team> findByGameProject(String gameProject);
}
