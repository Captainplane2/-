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
 * 约赛申请实体类
 */
@Data
@Entity
@Table(name = "match_application")
public class MatchApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId; // 关联的约赛ID
    
    private Long applicantTeamId; // 申请的战队ID

    /**
     * 状态：0 待审核, 1 已通过, 2 已拒绝
     */
    private Integer status = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
