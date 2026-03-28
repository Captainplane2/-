package com.esports.zds.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 选手信息DTO
 */
@Data
public class PlayerDTO {
    
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String university;
    private String role;
    private LocalDateTime createTime;
    
    // 战队信息列表
    private List<TeamInfo> teams;
    
    // 参与的游戏项目列表
    private List<String> gameProjects;
    
    /**
     * 战队信息内部类
     */
    @Data
    public static class TeamInfo {
        private Long id;
        private String name;
        private String role;
        private String gameProject;
    }
}
