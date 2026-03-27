package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Banner标题
    private String title;

    // 图片URL
    @Column(nullable = false)
    private String image;

    // 跳转链接
    private String url;

    // 排序权重 (数字越小越靠前)
    private Integer sortOrder = 0;

    // 状态 (0: 启用, 1: 停用)
    private Integer status = 0;

    // 创建时间
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
