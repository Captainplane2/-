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
 * 战队成员关系实体
 */
@Data
@Entity
@Table(name = "team_member")
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teamId; // 战队ID
    
    private Long userId; // 用户ID

    /**
     * 队内角色：0 成员, 1 队长
     */
    private Integer role = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime joinTime;
    
    /**
     * 游戏项目 (LOL, 王者荣耀, CS2等)
     */
    private String gameProject;
}
