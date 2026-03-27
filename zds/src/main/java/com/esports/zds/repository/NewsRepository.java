package com.esports.zds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.News;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    // 按创建时间倒序获取所有新闻
    List<News> findAllByOrderByCreateTimeDesc();

    // 根据游戏项目筛选并按时间倒序
    List<News> findByGameProjectOrderByCreateTimeDesc(String gameProject);
}
