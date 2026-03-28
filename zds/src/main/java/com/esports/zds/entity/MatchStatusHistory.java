package com.esports.zds.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 约战状态历史记录实体
 */
@Data
@Entity
@Table(name = "t_match_status_history")
public class MatchStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "match_room_id")
    private MatchRoom matchRoom;
    
    @Enumerated(EnumType.STRING)
    private MatchStatus fromStatus;
    
    @Enumerated(EnumType.STRING)
    private MatchStatus toStatus;
    
    private LocalDateTime changeTime;
    
    private Long changedBy; // 操作人ID
    
    private String changeReason;
    
    @PrePersist
    protected void onCreate() {
        changeTime = LocalDateTime.now();
    }
}