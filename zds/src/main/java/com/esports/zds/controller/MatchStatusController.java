package com.esports.zds.controller;

import com.esports.zds.entity.MatchRoom;
import com.esports.zds.service.MatchStatusService;
import com.esports.zds.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * 约战状态管理控制器
 */
@RestController
@RequestMapping("/api/match-status")
public class MatchStatusController {
    
    @Autowired
    private MatchStatusService matchStatusService;
    
    /**
     * 准备/取消准备
     */
    @PostMapping("/ready/{matchId}")
    public Result<MatchRoom> toggleReady(@PathVariable Long matchId, 
                                       @RequestParam Long userId,
                                       @RequestParam String teamType) {
        try {
            MatchRoom room = matchStatusService.toggleReady(matchId, userId, teamType);
            return Result.success("准备状态已更新", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * 开始比赛（倒计时结束后自动调用）
     */
    @PostMapping("/start/{matchId}")
    public Result<MatchRoom> startMatch(@PathVariable Long matchId) {
        try {
            MatchRoom room = matchStatusService.startMatch(matchId);
            return Result.success("比赛开始", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * 确认比赛结束
     */
    @PostMapping("/finish-confirm/{matchId}")
    public Result<MatchRoom> confirmFinish(@PathVariable Long matchId, 
                                         @RequestParam Long userId) {
        try {
            MatchRoom room = matchStatusService.confirmFinish(matchId, userId);
            return Result.success("结束确认已记录", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * 完成比赛（结束倒计时结束后自动调用）
     */
    @PostMapping("/finish/{matchId}")
    public Result<MatchRoom> finishMatch(@PathVariable Long matchId) {
        try {
            MatchRoom room = matchStatusService.finishMatch(matchId);
            return Result.success("比赛结束", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * 获取约战状态详情
     */
    @GetMapping("/detail/{matchId}")
    public Result<MatchStatusDetail> getStatusDetail(@PathVariable Long matchId) {
        try {
            MatchStatusDetail detail = new MatchStatusDetail();
            // 这里需要实现获取详细状态信息的逻辑
            return Result.success(detail);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
    
    /**
     * 状态详情DTO
     */
    public static class MatchStatusDetail {
        private Long matchId;
        private String matchStatus;
        private Boolean hostTeamReady;
        private Boolean guestTeamReady;
        private Integer finishConfirmCount;
        private Integer countdownSeconds;
        private LocalDateTime countdownStartTime;
        
        // getter和setter方法
        public Long getMatchId() { return matchId; }
        public void setMatchId(Long matchId) { this.matchId = matchId; }
        
        public String getMatchStatus() { return matchStatus; }
        public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }
        
        public Boolean getHostTeamReady() { return hostTeamReady; }
        public void setHostTeamReady(Boolean hostTeamReady) { this.hostTeamReady = hostTeamReady; }
        
        public Boolean getGuestTeamReady() { return guestTeamReady; }
        public void setGuestTeamReady(Boolean guestTeamReady) { this.guestTeamReady = guestTeamReady; }
        
        public Integer getFinishConfirmCount() { return finishConfirmCount; }
        public void setFinishConfirmCount(Integer finishConfirmCount) { this.finishConfirmCount = finishConfirmCount; }
        
        public Integer getCountdownSeconds() { return countdownSeconds; }
        public void setCountdownSeconds(Integer countdownSeconds) { this.countdownSeconds = countdownSeconds; }
        
        public LocalDateTime getCountdownStartTime() { return countdownStartTime; }
        public void setCountdownStartTime(LocalDateTime countdownStartTime) { this.countdownStartTime = countdownStartTime; }
    }
}