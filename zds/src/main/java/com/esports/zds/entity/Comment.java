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
 * 评论实体类
 */
@Data
@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId; // 关联的帖子ID
    
    private Long userId; // 评论人ID

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 评论内容

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
