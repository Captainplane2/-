package com.esports.zds.controller;

import com.esports.zds.service.UserIdentityService;
import com.esports.zds.entity.User;
import com.esports.zds.service.UserService;
import com.esports.zds.common.SecurityUtils;
import com.esports.zds.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user/identity")
public class UserIdentityController {

    @Autowired
    private UserIdentityService userIdentityService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户在指定游戏板块的身份
     * @param gameProject 游戏项目
     * @return 身份标签
     */
    @GetMapping("/game/{gameProject}")
    public Result<Map<String, String>> getIdentityInGameProject(@PathVariable String gameProject) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.success("获取身份成功", Map.of("identity", null));
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.success("获取身份成功", Map.of("identity", null));
        }
        
        String identity = userIdentityService.getUserIdentityInGameProject(user.getId(), gameProject);
        return Result.success("获取身份成功", Map.of("identity", identity));
    }

    /**
     * 获取用户在所有游戏板块的身份
     * @return 按游戏板块分组的身份标签
     */
    @GetMapping("/all")
    public Result<Map<String, String>> getAllIdentities() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.success("获取身份成功", Map.of());
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.success("获取身份成功", Map.of());
        }
        
        Map<String, String> identities = userIdentityService.getUserIdentitiesAcrossGameProjects(user.getId());
        return Result.success("获取身份成功", identities);
    }
}
