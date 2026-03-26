package com.esports.zds.service;

import com.esports.zds.entity.User;

import java.util.Map;

/**
 * 用户业务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     * @param user 用户信息
     */
    void register(User user);

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 包含 Token 和用户信息的 Map
     */
    Map<String, Object> login(String username, String password);

    /**
     * 根据ID获取用户信息
     */
    User getById(Long id);

    /**
     * 更新个人资料
     */
    void updateProfile(User user);
}
