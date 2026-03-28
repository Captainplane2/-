package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.entity.MatchRoom;
import com.esports.zds.entity.User;
import com.esports.zds.repository.UserRepository;
import com.esports.zds.service.MatchRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match-room")
public class MatchRoomController {

    @Autowired
    private MatchRoomService matchRoomService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public Result<MatchRoom> createRoom(@RequestBody MatchRoom room) {
        MatchRoom created = matchRoomService.createRoom(room);
        return Result.success("房间发布成功", created);
    }

    @GetMapping("/list")
    public Result<List<MatchRoom>> listRooms(@RequestParam(required = false) String gameProject, @RequestParam(required = false) String keyword) {
        return Result.success(matchRoomService.listRooms(gameProject, keyword));
    }

    @PostMapping("/join/{roomId}")
    public Result<MatchRoom> joinRoom(@PathVariable Long roomId, 
                                    @RequestParam Long guestTeamId, 
                                    @RequestParam String guestTeamName) {
        try {
            MatchRoom room = matchRoomService.joinRoom(roomId, guestTeamId, guestTeamName);
            return Result.success("应战成功", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/finish/{roomId}")
    public Result<MatchRoom> finishRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            MatchRoom room = matchRoomService.finishRoom(roomId, userId);
            return Result.success("约战已结束", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/cancel/{roomId}")
    public Result<MatchRoom> cancelRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            // 从数据库获取用户最新角色信息（避免token中角色信息过期）
            User user = userRepository.findById(userId).orElse(null);
            String userRole = user != null ? user.getRole() : null;
            MatchRoom room = matchRoomService.cancelRoom(roomId, userId, userRole);
            return Result.success("约战已取消", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * [管理端] 获取所有约赛列表
     */
    @GetMapping("/admin/list")
    public Result<List<MatchRoom>> listAllRooms() {
        try {
            return Result.success(matchRoomService.listAllRooms());
        } catch (Exception e) {
            return Result.error(500, "获取约赛记录失败");
        }
    }

    /**
     * [管理端] 更新约赛信息
     */
    @PutMapping("/update")
    public Result<MatchRoom> updateRoom(@RequestBody MatchRoom room) {
        try {
            MatchRoom updated = matchRoomService.updateRoom(room);
            return Result.success("约赛信息已更新", updated);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * [管理端] 删除约赛记录
     */
    @PostMapping("/admin/delete/{roomId}")
    public Result<String> deleteRoom(@PathVariable Long roomId) {
        try {
            matchRoomService.deleteRoom(roomId);
            return Result.success("约赛记录已删除", null);
        } catch (Exception e) {
            return Result.error(500, "删除约赛记录失败");
        }
    }
}
