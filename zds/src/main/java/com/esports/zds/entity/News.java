package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 电竞新闻实体类
 */
@Data
@Entity
@Table(name = "t_news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 新闻标题
    @Column(nullable = false)
    private String title;

    // 新闻摘要
    private String summary;

    // 封面图片URL
    private String coverImage;

    // 新闻正文 (富文本内容，使用 LONGTEXT 存储)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 游戏项目分类 (如：LOL, CS2, 王者荣耀)
    private String gameProject;

    // 浏览量
    private Integer viewCount = 0;

    // 作者/发布者ID
    private Long authorId;

    // 发布时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
