package com.esports.zds.repository;

import com.esports.zds.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 获取帖子的所有评论
    List<Comment> findByPostIdOrderByCreateTimeAsc(Long postId);
}
