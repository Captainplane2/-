package com.esports.zds.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 系统留言实体类
 */
@Data
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 留言用户ID

    @Column(columnDefinition = "TEXT")
    private String content; // 留言内容

    @Column(columnDefinition = "TEXT")
    private String reply;   // 管理员回复

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime replyTime;

    /**
     * 状态：0 未回复, 1 已回复
     */
    private Integer status = 0;
}
