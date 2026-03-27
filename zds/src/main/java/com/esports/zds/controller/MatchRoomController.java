package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.entity.MatchRoom;
import com.esports.zds.service.MatchRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match-room")
public class MatchRoomController {

    @Autowired
    private MatchRoomService matchRoomService;

    @PostMapping("/create")
    public Result<MatchRoom> createRoom(@RequestBody MatchRoom room) {
        MatchRoom created = matchRoomService.createRoom(room);
        return Result.success("房间发布成功", created);
    }

    @GetMapping("/list")
    public Result<List<MatchRoom>> listRooms(@RequestParam(required = false) String gameProject) {
        return Result.success(matchRoomService.listRooms(gameProject));
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
            MatchRoom room = matchRoomService.cancelRoom(roomId, userId);
            return Result.success("约战已取消", room);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
