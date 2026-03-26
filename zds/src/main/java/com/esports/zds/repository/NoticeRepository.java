package com.esports.zds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.Notice;

/**
 * 公告数据访问接口
 */
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    /**
     * 查询所有上架的公告，并按时间倒序
     */
    List<Notice> findByStatusOrderByCreateTimeDesc(Integer status);
}
