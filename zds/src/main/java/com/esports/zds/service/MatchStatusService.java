package com.esports.zds.service;

import com.esports.zds.entity.MatchRoom;
import com.esports.zds.entity.MatchStatus;
import com.esports.zds.entity.MatchStatusHistory;
import com.esports.zds.entity.User;
import com.esports.zds.repository.MatchRoomRepository;
import com.esports.zds.repository.MatchStatusHistoryRepository;
import com.esports.zds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class MatchStatusService {
    
    @Autowired
    private MatchRoomRepository matchRoomRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MatchStatusHistoryRepository matchStatusHistoryRepository;
    
    /**
     * 准备/取消准备
     */
    @Transactional
    public MatchRoom toggleReady(Long matchId, Long userId, String teamType) {
        MatchRoom room = matchRoomRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("约战不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：必须是队长
        if (!isTeamLeader(room, user, teamType)) {
            throw new RuntimeException("只有队长可以操作准备状态");
        }
        
        // 状态检查：必须是已应战状态
        if (room.getMatchStatus() != MatchStatus.READY) {
            throw new RuntimeException("当前状态不允许准备操作");
        }
        
        // 检查是否已过期
        if (isMatchExpired(room)) {
            updateMatchStatus(room, MatchStatus.EXPIRED, userId, "比赛已过期");
            throw new RuntimeException("比赛已过期，无法操作");
        }
        
        // 更新准备状态
        boolean newReadyState;
        if ("host".equals(teamType)) {
            newReadyState = !room.getHostTeamReady();
            room.setHostTeamReady(newReadyState);
        } else {
            newReadyState = !room.getGuestTeamReady();
            room.setGuestTeamReady(newReadyState);
        }
        
        // 记录状态变更
        String action = newReadyState ? "准备" : "取消准备";
        recordStatusChange(room, room.getMatchStatus(), room.getMatchStatus(), userId, 
            teamType + "战队" + action);
        
        // 如果双方都准备，开始倒计时
        if (room.getHostTeamReady() && room.getGuestTeamReady()) {
            room.setCountdownStartTime(LocalDateTime.now());
            room.setCountdownSeconds(30);
        } else {
            // 任意一方取消准备，重置倒计时
            room.setCountdownStartTime(null);
            room.setCountdownSeconds(null);
        }
        
        return matchRoomRepository.save(room);
    }
    
    /**
     * 开始比赛（倒计时结束后自动调用）
     */
    @Transactional
    public MatchRoom startMatch(Long matchId) {
        MatchRoom room = matchRoomRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("约战不存在"));
        
        // 状态检查
        if (room.getMatchStatus() != MatchStatus.READY || 
            !room.getHostTeamReady() || !room.getGuestTeamReady()) {
            throw new RuntimeException("无法开始比赛");
        }
        
        // 检查倒计时是否已结束
        if (room.getCountdownStartTime() == null || room.getCountdownSeconds() == null) {
            throw new RuntimeException("倒计时未开始");
        }
        
        long elapsedSeconds = ChronoUnit.SECONDS.between(
            room.getCountdownStartTime(), LocalDateTime.now());
        
        if (elapsedSeconds < room.getCountdownSeconds()) {
            throw new RuntimeException("倒计时尚未结束");
        }
        
        // 更新状态
        updateMatchStatus(room, MatchStatus.IN_PROGRESS, null, "比赛开始");
        room.setActualStartTime(LocalDateTime.now());
        room.setCountdownStartTime(null);
        room.setCountdownSeconds(null);
        
        return matchRoomRepository.save(room);
    }
    
    /**
     * 确认比赛结束
     */
    @Transactional
    public MatchRoom confirmFinish(Long matchId, Long userId) {
        MatchRoom room = matchRoomRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("约战不存在"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 权限检查：必须是队长
        if (!isTeamLeader(room, user, "host") && !isTeamLeader(room, user, "guest")) {
            throw new RuntimeException("只有队长可以确认比赛结束");
        }
        
        // 状态检查：必须是正在比赛状态
        if (room.getMatchStatus() != MatchStatus.IN_PROGRESS) {
            throw new RuntimeException("当前状态不允许结束确认");
        }
        
        // 更新结束确认计数
        room.setFinishConfirmCount(room.getFinishConfirmCount() + 1);
        
        // 记录状态变更
        recordStatusChange(room, room.getMatchStatus(), room.getMatchStatus(), userId, 
            "结束确认计数: " + room.getFinishConfirmCount());
        
        // 如果双方都确认结束，开始结束倒计时
        if (room.getFinishConfirmCount() >= 2) {
            room.setCountdownStartTime(LocalDateTime.now());
            room.setCountdownSeconds(10);
        }
        
        return matchRoomRepository.save(room);
    }
    
    /**
     * 完成比赛（结束倒计时结束后自动调用）
     */
    @Transactional
    public MatchRoom finishMatch(Long matchId) {
        MatchRoom room = matchRoomRepository.findById(matchId)
            .orElseThrow(() -> new RuntimeException("约战不存在"));
        
        // 状态检查
        if (room.getMatchStatus() != MatchStatus.IN_PROGRESS || room.getFinishConfirmCount() < 2) {
            throw new RuntimeException("无法完成比赛");
        }
        
        // 检查倒计时是否已结束
        if (room.getCountdownStartTime() == null || room.getCountdownSeconds() == null) {
            throw new RuntimeException("结束倒计时未开始");
        }
        
        long elapsedSeconds = ChronoUnit.SECONDS.between(
            room.getCountdownStartTime(), LocalDateTime.now());
        
        if (elapsedSeconds < room.getCountdownSeconds()) {
            throw new RuntimeException("结束倒计时尚未结束");
        }
        
        // 更新状态
        updateMatchStatus(room, MatchStatus.FINISHED, null, "比赛结束");
        room.setActualEndTime(LocalDateTime.now());
        room.setCountdownStartTime(null);
        room.setCountdownSeconds(null);
        room.setFinishConfirmCount(0);
        
        return matchRoomRepository.save(room);
    }
    
    /**
     * 检查并更新过期状态（定时任务）
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Transactional
    public void checkAndUpdateExpiredMatches() {
        List<MatchRoom> activeMatches = matchRoomRepository.findByMatchStatusIn(
            List.of(MatchStatus.READY, MatchStatus.IN_PROGRESS));
        
        for (MatchRoom room : activeMatches) {
            if (isMatchExpired(room)) {
                updateMatchStatus(room, MatchStatus.EXPIRED, null, "比赛已过期");
                matchRoomRepository.save(room);
            }
        }
    }
    
    /**
     * 检查比赛是否已过期（超过开赛时间30分钟）
     */
    private boolean isMatchExpired(MatchRoom room) {
        if (room.getMatchTime() == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = room.getMatchTime().plusMinutes(30);
        
        return now.isAfter(expireTime);
    }
    
    /**
     * 检查用户是否是队长
     */
    private boolean isTeamLeader(MatchRoom room, User user, String teamType) {
        if ("host".equals(teamType)) {
            return user.getId().equals(room.getHostId());
        } else if ("guest".equals(teamType)) {
            return user.getId().equals(room.getGuestId());
        }
        return false;
    }
    
    /**
     * 更新比赛状态
     */
    private void updateMatchStatus(MatchRoom room, MatchStatus newStatus, Long userId, String reason) {
        MatchStatus oldStatus = room.getMatchStatus();
        room.setMatchStatus(newStatus);
        
        // 记录状态变更历史
        recordStatusChange(room, oldStatus, newStatus, userId, reason);
    }
    
    /**
     * 记录状态变更历史
     */
    private void recordStatusChange(MatchRoom room, MatchStatus fromStatus, MatchStatus toStatus, 
                                  Long userId, String reason) {
        MatchStatusHistory history = new MatchStatusHistory();
        history.setMatchRoom(room);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(userId);
        history.setChangeReason(reason);
        
        matchStatusHistoryRepository.save(history);
    }
}