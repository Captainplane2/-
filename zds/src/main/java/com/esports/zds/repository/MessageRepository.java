package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.Message;

/**
 * 留言数据访问接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 根据用户ID查询留言
     */
    List<Message> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 查询所有未回复的留言
     */
    List<Message> findByStatus(Integer status);
}
