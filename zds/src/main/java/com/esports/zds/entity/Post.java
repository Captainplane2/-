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
 * 帖子实体类
 */
@Data
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private Long userId; // 发帖人ID

    private String gameProject; // 所属游戏项目

    private Integer views = 0;   // 浏览量
    private Integer likes = 0;   // 点赞数
    private Integer comments = 0; // 评论数

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    /**
     * 状态：0 正常, 1 已删除
     */
    private Integer status = 0;
}
