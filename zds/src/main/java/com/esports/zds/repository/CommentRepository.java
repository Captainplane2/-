package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.Comment;

/**
 * 评论数据访问接口
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 根据帖子ID查询评论，并按时间升序
     */
    List<Comment> findByPostIdOrderByCreateTimeAsc(Long postId);
}
