package com.esports.zds.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esports.zds.common.JwtUtils;
import com.esports.zds.entity.User;
import com.esports.zds.repository.UserRepository;
import com.esports.zds.service.UserService;

import cn.hutool.crypto.digest.DigestUtil;

/**
 * 用户业务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void register(User user) {
        // 1. 检查邮箱是否存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 2. 密码加密存储 (使用 BCrypt 或 MD5+盐，这里演示简单使用 MD5)
        user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        
        // 3. 设置默认角色和状态
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        user.setStatus(0); // 待认证

        userRepository.save(user);
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        // 1. 查询用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2. 校验密码
        if (!user.getPassword().equals(DigestUtil.md5Hex(password))) {
            throw new RuntimeException("密码错误");
        }

        // 3. 生成 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = jwtUtils.generateToken(claims);

        // 4. 封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        
        return result;
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void updateProfile(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 仅更新允许修改的字段
        existingUser.setNickname(user.getNickname());
        existingUser.setAvatar(user.getAvatar());
        existingUser.setPhone(user.getPhone());
        existingUser.setRealName(user.getRealName());
        existingUser.setGender(user.getGender());
        existingUser.setQq(user.getQq());
        existingUser.setWechat(user.getWechat());
        existingUser.setSignature(user.getSignature());
        
        userRepository.save(existingUser);
    }
}
