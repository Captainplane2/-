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
 * 战队加入申请实体类
 */
@Data
@Entity
@Table(name = "team_application")
public class TeamApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teamId; // 目标战队ID
    
    private Long userId; // 申请人ID

    private String reason; // 申请理由

    /**
     * 状态：0 待审核, 1 已通过, 2 已拒绝
     */
    private Integer status = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
