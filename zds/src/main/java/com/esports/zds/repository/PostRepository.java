package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.Post;

/**
 * 帖子数据访问接口
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    /**
     * 按游戏项目查询帖子，并按时间倒序
     */
    List<Post> findByGameProjectAndStatusOrderByCreateTimeDesc(String gameProject, Integer status);

    /**
     * 按用户ID查询帖子
     */
    List<Post> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Integer status);

    /**
     * 查询所有正常状态的帖子，并按时间倒序
     */
    List<Post> findByStatusOrderByCreateTimeDesc(Integer status);
}
