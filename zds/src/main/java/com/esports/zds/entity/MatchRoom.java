package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 约战房间实体类
 */
@Data
@Entity
@Table(name = "t_match_room")
public class MatchRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 约战项目 (如：LOL, CS2, 王者荣耀)
    @Column(nullable = false)
    private String gameProject;

    // 房间标题 (如：XX大学寻求白金水平约战)
    private String title;

    // 约战类型 (0: 线上, 1: 线下)
    private Integer type = 0;

    // 房间状态 (0: 招募中, 1: 已应战, 2: 已结束, 3: 已取消)
    private Integer status = 0;

    // 发起战队ID
    @Column(nullable = false)
    private Long hostTeamId;

    // 发起战队名称
    private String hostTeamName;

    // 发起人(队长)ID
    private Long hostId;

    // 发起人学校
    private String hostUniversity;

    // 发起战队logo
    private String hostTeamLogo;

    // 发起战队队长昵称
    private String hostLeaderNickname;

    // 应战战队ID (待匹配)
    private Long guestTeamId;

    // 应战战队名称
    private String guestTeamName;
    
    // 应战战队logo
    private String guestTeamLogo;
    
    // 应战方学校
    private String guestUniversity;
    
    // 应战方(队长)ID
    private Long guestId;
    
    // 应战战队队长昵称
    private String guestLeaderNickname;

    // 约战时间
    private LocalDateTime matchTime;

    // 约战地点 (线下约战时必填)
    private String location;

    // 备注信息
    private String description;

    // 创建时间
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
