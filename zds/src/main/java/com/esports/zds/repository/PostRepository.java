package com.esports.zds.repository;

import com.esports.zds.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 按板块和创建时间倒序获取帖子
    List<Post> findByCategoryOrderByCreateTimeDesc(String category);
    
    // 按项目和状态获取帖子
    List<Post> findByGameProjectAndStatusOrderByCreateTimeDesc(String gameProject, Integer status);
    
    // 按状态获取帖子并按时间倒序
    List<Post> findByStatusOrderByCreateTimeDesc(Integer status);

    // 获取某个用户的指定状态帖子
    List<Post> findByUserIdAndStatusOrderByCreateTimeDesc(Long userId, Integer status);

    // 按项目和创建时间倒序获取帖子 (原有兼容)
    List<Post> findByGameProjectOrderByCreateTimeDesc(String gameProject);
    
    // 获取所有帖子并按创建时间倒序
    List<Post> findAllByOrderByCreateTimeDesc();

    // 获取用户的帖子
    List<Post> findByUserIdOrderByCreateTimeDesc(Long userId);
}
