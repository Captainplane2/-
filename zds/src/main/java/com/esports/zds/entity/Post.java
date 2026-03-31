package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社区帖子实体类
 */
@Data
@Entity
@Table(name = "t_post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 帖子标题
    @Column(nullable = false)
    private String title;

    // 帖子内容 (富文本)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    // 状态 (0: 正常, 1: 已删除)
    private Integer status = 0;

    // 所属板块 (如：赛事讨论、技术交流、组队开黑)
    private String category;

    // 游戏项目关联
    private String gameProject;

    // 作者ID
    private Long userId;

    // 作者昵称
    private String nickname;

    // 作者学校
    private String university;
    
    // 作者头像
    private String avatar;

    // 浏览量 (保持与旧代码一致的 setViews/getViews)
    private Integer views = 0;

    // 点赞数 (保持与旧代码一致的 setLikes/getLikes)
    private Integer likes = 0;

    // 评论数 (保持与旧代码一致的 setComments/getComments)
    private Integer comments = 0;

    // 创建时间
    private LocalDateTime createTime = LocalDateTime.now();

    // 更新时间
    private LocalDateTime updateTime = LocalDateTime.now();

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
