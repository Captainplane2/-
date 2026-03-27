package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子评论实体类
 */
@Data
@Entity
@Table(name = "t_comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联的帖子ID
    @Column(nullable = false)
    private Long postId;

    // 评论内容
    @Column(nullable = false, length = 1000)
    private String content;

    // 评论者ID
    private Long userId;

    // 评论者昵称
    private String nickname;

    // 创建时间
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
