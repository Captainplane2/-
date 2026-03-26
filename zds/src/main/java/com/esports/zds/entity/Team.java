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
 * 战队实体类
 */
@Data
@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 战队名称

    private String logo; // 战队Logo

    private String university; // 所属高校

    private String gameProject; // 游戏项目 (LOL, 王者荣耀, CS2等)

    private String description; // 战队简介

    private Long leaderId; // 队长ID (关联User)

    private Integer memberCount = 1; // 成员数量

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    /**
     * 战队状态：0 正常, 1 解散
     */
    private Integer status = 0;
}
