package com.esports.zds.service;

import com.esports.zds.entity.MatchRoom;
import com.esports.zds.entity.Team;
import com.esports.zds.entity.User;
import com.esports.zds.repository.MatchRoomRepository;
import com.esports.zds.repository.MatchStatusHistoryRepository;
import com.esports.zds.repository.UserRepository;
import com.esports.zds.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchRoomService {

    @Autowired
    private MatchRoomRepository matchRoomRepository;
    
    @Autowired
    private MatchStatusHistoryRepository matchStatusHistoryRepository;
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 创建约战房间
     */
    public MatchRoom createRoom(MatchRoom room) {
        room.setStatus(0); // 招募中
        room.setMatchStatus(com.esports.zds.entity.MatchStatus.WAITING); // 同步设置matchStatus字段
        return matchRoomRepository.save(room);
    }

    /**
     * 获取约战大厅列表
     */
    public List<MatchRoom> listRooms(String gameProject, String keyword) {
        List<MatchRoom> rooms;
        // 获取状态为0（招募中）和1（已应战）的约战
        List<Integer> activeStatuses = List.of(0, 1);
        if (gameProject != null && !gameProject.isEmpty() && !"全部".equals(gameProject)) {
            rooms = matchRoomRepository.findByGameProjectAndStatusInOrderByCreateTimeDesc(gameProject, activeStatuses);
        } else {
            rooms = matchRoomRepository.findByStatusInOrderByCreateTimeDesc(activeStatuses);
        }
        
        // 刷新战队信息
        for (MatchRoom room : rooms) {
            // 刷新发起方战队信息
            if (room.getHostTeamId() != null) {
                Team hostTeam = teamService.getById(room.getHostTeamId());
                if (hostTeam != null) {
                    room.setHostTeamName(hostTeam.getName());
                    room.setHostTeamLogo(hostTeam.getLogo());
                    // 获取发起方队长信息
                    if (hostTeam.getLeaderId() != null) {
                        userRepository.findById(hostTeam.getLeaderId()).ifPresent(user -> {
                            room.setHostUniversity(user.getUniversity());
                            room.setHostLeaderNickname(user.getNickname());
                        });
                    }
                }
            }
            // 刷新应战方战队信息
            if (room.getGuestTeamId() != null) {
                Team guestTeam = teamService.getById(room.getGuestTeamId());
                if (guestTeam != null) {
                    room.setGuestTeamName(guestTeam.getName());
                    room.setGuestTeamLogo(guestTeam.getLogo());
                    // 获取应战方队长信息
                    if (guestTeam.getLeaderId() != null) {
                        userRepository.findById(guestTeam.getLeaderId()).ifPresent(user -> {
                            room.setGuestUniversity(user.getUniversity());
                            room.setGuestId(user.getId());
                            room.setGuestLeaderNickname(user.getNickname());
                        });
                    }
                }
            }
        }
        
        // 按关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            rooms = rooms.stream()
                .filter(room -> {
                    // 按约战标题搜索
                    if (room.getTitle() != null && room.getTitle().toLowerCase().contains(lowerKeyword)) {
                        return true;
                    }
                    // 按发起方高校名称搜索
                    if (room.getHostUniversity() != null && room.getHostUniversity().toLowerCase().contains(lowerKeyword)) {
                        return true;
                    }
                    // 按应战方高校名称搜索
                    if (room.getGuestUniversity() != null && room.getGuestUniversity().toLowerCase().contains(lowerKeyword)) {
                        return true;
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        return rooms;
    }

    /**
     * 获取所有约战记录 (管理端或历史查询)
     */
    public List<MatchRoom> listAllRooms() {
        List<MatchRoom> rooms = matchRoomRepository.findAllByOrderByCreateTimeDesc();
        
        // 刷新战队信息
        for (MatchRoom room : rooms) {
            // 刷新发起方战队信息
            if (room.getHostTeamId() != null) {
                Team hostTeam = teamService.getById(room.getHostTeamId());
                if (hostTeam != null) {
                    room.setHostTeamName(hostTeam.getName());
                    room.setHostTeamLogo(hostTeam.getLogo());
                    // 获取发起方队长信息
                    if (hostTeam.getLeaderId() != null) {
                        userRepository.findById(hostTeam.getLeaderId()).ifPresent(user -> {
                            room.setHostUniversity(user.getUniversity());
                            room.setHostLeaderNickname(user.getNickname());
                        });
                    }
                }
            }
            // 刷新应战方战队信息
            if (room.getGuestTeamId() != null) {
                Team guestTeam = teamService.getById(room.getGuestTeamId());
                if (guestTeam != null) {
                    room.setGuestTeamName(guestTeam.getName());
                    room.setGuestTeamLogo(guestTeam.getLogo());
                    // 获取应战方队长信息
                    if (guestTeam.getLeaderId() != null) {
                        userRepository.findById(guestTeam.getLeaderId()).ifPresent(user -> {
                            room.setGuestUniversity(user.getUniversity());
                            room.setGuestId(user.getId());
                            room.setGuestLeaderNickname(user.getNickname());
                        });
                    }
                }
            }
        }
        
        return rooms;
    }

    /**
     * 应战
     */
    public MatchRoom joinRoom(Long roomId, Long guestTeamId, String guestTeamName) {
        MatchRoom room = matchRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("房间不存在"));
        if (room.getStatus() != 0) {
            throw new RuntimeException("该房间已被应战或关闭");
        }
        if (room.getHostTeamId().equals(guestTeamId)) {
            throw new RuntimeException("不能应战自己的房间");
        }
        
        // 获取应战战队信息，设置应战队长ID
        Team guestTeam = teamService.getById(guestTeamId);
        if (guestTeam == null) {
            throw new RuntimeException("战队不存在");
        }
        
        room.setGuestTeamId(guestTeamId);
        room.setGuestTeamName(guestTeamName);
        room.setGuestTeamLogo(guestTeam.getLogo());
        room.setGuestUniversity(guestTeam.getUniversity());
        
        // 设置应战队长ID和昵称
        if (guestTeam.getLeaderId() != null) {
            userRepository.findById(guestTeam.getLeaderId()).ifPresent(user -> {
                room.setGuestId(user.getId());
                room.setGuestLeaderNickname(user.getNickname());
            });
        }
        
        room.setStatus(1); // 已应战，待开打
        room.setMatchStatus(com.esports.zds.entity.MatchStatus.READY); // 同步更新matchStatus字段
        return matchRoomRepository.save(room);
    }

    /**
     * 结束约战 (由发起人操作)
     */
    public MatchRoom finishRoom(Long roomId, Long userId) {
        MatchRoom room = matchRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("房间不存在"));
        if (!room.getHostId().equals(userId)) {
            throw new RuntimeException("只有发起人可以结束约战");
        }
        room.setStatus(2); // 已结束
        room.setMatchStatus(com.esports.zds.entity.MatchStatus.FINISHED); // 同步更新matchStatus字段
        return matchRoomRepository.save(room);
    }

    /**
     * 取消约战 (由发起人操作或管理员操作)
     */
    public MatchRoom cancelRoom(Long roomId, Long userId, String userRole) {
        MatchRoom room = matchRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("房间不存在"));
        // 检查权限：管理员或发起人
        if (!"ROLE_ADMIN".equals(userRole) && !room.getHostId().equals(userId)) {
            throw new RuntimeException("只有发起人可以取消约战");
        }
        // 管理员可以取消任何状态的约战，普通用户只能取消招募中的约战
        if (!"ROLE_ADMIN".equals(userRole) && room.getStatus() != 0) {
            throw new RuntimeException("该房间已被应战或关闭，无法取消");
        }
        room.setStatus(3); // 已取消
        room.setMatchStatus(com.esports.zds.entity.MatchStatus.CANCELLED); // 同步更新matchStatus字段
        return matchRoomRepository.save(room);
    }

    /**
     * 修复约战状态（临时方法，用于修复历史数据）
     */
    public MatchRoom fixMatchStatus(Long roomId) {
        MatchRoom room = matchRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("房间不存在"));
        
        // 根据status字段同步matchStatus字段
        if (room.getStatus() == 0) {
            room.setMatchStatus(com.esports.zds.entity.MatchStatus.WAITING);
        } else if (room.getStatus() == 1) {
            room.setMatchStatus(com.esports.zds.entity.MatchStatus.READY);
        } else if (room.getStatus() == 2) {
            room.setMatchStatus(com.esports.zds.entity.MatchStatus.FINISHED);
        } else if (room.getStatus() == 3) {
            room.setMatchStatus(com.esports.zds.entity.MatchStatus.CANCELLED);
        }
        
        // 修复应战方队长ID（如果guestTeamId存在但guestId为空）
        if (room.getGuestTeamId() != null && room.getGuestId() == null) {
            Team guestTeam = teamService.getById(room.getGuestTeamId());
            if (guestTeam != null && guestTeam.getLeaderId() != null) {
                userRepository.findById(guestTeam.getLeaderId()).ifPresent(user -> {
                    room.setGuestId(user.getId());
                    room.setGuestLeaderNickname(user.getNickname());
                    room.setGuestUniversity(guestTeam.getUniversity());
                    room.setGuestTeamLogo(guestTeam.getLogo());
                    System.out.println("修复应战队长ID - guestId: " + user.getId() + ", guestLeaderNickname: " + user.getNickname());
                });
            }
        }
        
        System.out.println("修复约战状态 - ID: " + roomId + ", status: " + room.getStatus() + ", matchStatus: " + room.getMatchStatus() + ", guestId: " + room.getGuestId());
        return matchRoomRepository.save(room);
    }

    /**
     * 更新约战信息
     */
    public MatchRoom updateRoom(MatchRoom room) {
        MatchRoom existingRoom = matchRoomRepository.findById(room.getId()).orElseThrow(() -> new RuntimeException("房间不存在"));
        // 更新字段
        existingRoom.setGameProject(room.getGameProject());
        existingRoom.setTitle(room.getTitle());
        existingRoom.setType(room.getType());
        existingRoom.setMatchTime(room.getMatchTime());
        existingRoom.setLocation(room.getLocation());
        existingRoom.setDescription(room.getDescription());
        return matchRoomRepository.save(existingRoom);
    }

    /**
     * [管理端] 删除约战记录
     */
    @Transactional
    public void deleteRoom(Long roomId) {
        try {
            // 先删除相关的状态历史记录
            matchStatusHistoryRepository.deleteByMatchRoomId(roomId);
            // 再删除约战记录
            matchRoomRepository.deleteById(roomId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除约战记录失败: " + e.getMessage());
        }
    }
}
