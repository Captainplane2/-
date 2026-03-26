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
 * 约赛信息实体类
 */
@Data
@Entity
@Table(name = "match_info")
public class MatchInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long hostTeamId; // 发起战队ID
    
    private Long guestTeamId; // 接受战队ID (对手)

    private String gameProject; // 游戏项目

    private LocalDateTime matchTime; // 比赛时间

    private String mode; // 比赛模式 (线上/线下)

    private String requirement; // 约赛要求/备注

    /**
     * 约赛类型：0 公开大厅, 1 定向约赛
     */
    private Integer type;

    /**
     * 约赛状态：0 待匹配, 1 已匹配, 2 待开赛, 3 进行中, 4 已结束, 5 已拒绝
     */
    private Integer status = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
