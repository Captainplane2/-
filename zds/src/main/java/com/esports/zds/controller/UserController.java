package com.esports.zds.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.esports.zds.common.Result;
import com.esports.zds.entity.User;
import com.esports.zds.entity.TeamMember;
import com.esports.zds.repository.UserRepository;
import com.esports.zds.repository.TeamRepository;
import com.esports.zds.repository.TeamMemberRepository;
import com.esports.zds.service.UserService;
import com.esports.zds.config.UploadConfig;

/**
 * 用户模块控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UploadConfig uploadConfig;

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功", null);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        Map<String, Object> data = userService.login(username, password);
        return Result.success("登录成功", data);
    }

    /**
     * 获取个人资料
     */
    @GetMapping("/{id}")
    public Result<User> getInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 修改个人资料
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody User user) {
        userService.updateProfile(user);
        return Result.success("修改成功", null);
    }

    /**
     * [管理端] 获取所有用户列表 (多战队矩阵版)
     */
    @GetMapping("/admin/list")
    public Result<List<Map<String, Object>>> listAll() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (User user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("nickname", user.getNickname());
            map.put("university", user.getUniversity());
            map.put("role", user.getRole());
            map.put("status", user.getStatus());
            map.put("createTime", user.getCreateTime());

            // 聚合所有参加的项目和战队
            List<TeamMember> relations = teamMemberRepository.findByUserId(user.getId());
            List<Map<String, Object>> projects = new ArrayList<>();
            for (TeamMember rel : relations) {
                teamRepository.findById(rel.getTeamId()).ifPresent(team -> {
                    if (team.getStatus() == 0) { // 仅显示正常状态战队
                        Map<String, Object> pMap = new HashMap<>();
                        pMap.put("teamId", team.getId());
                        pMap.put("projectName", team.getGameProject());
                        pMap.put("teamName", team.getName());
                        projects.add(pMap);
                    }
                });
            }
            map.put("projects", projects);
            resultList.add(map);
        }
        return Result.success(resultList);
    }

    /**
     * [管理端] 删除/禁用用户
     */
    @PostMapping("/admin/status/{id}")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
        return Result.success("操作成功", null);
    }

    /**
     * [管理端] 修改用户角色
     */
    @PostMapping("/admin/role/{id}")
    public Result<String> updateRole(@PathVariable Long id, @RequestParam String role) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setRole(role);
        userRepository.save(user);
        return Result.success("权限设置成功", null);
    }

    /**
     * [管理端] 修改用户所属高校
     */
    @PostMapping("/admin/university/{id}")
    public Result<String> updateUniversity(@PathVariable Long id, @RequestParam String university) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setUniversity(university);
        userRepository.save(user);
        return Result.success("所属高校更新成功", null);
    }

    /**
     * [管理端] 删除用户
     */
    @PostMapping("/admin/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return Result.success("用户删除成功", null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, String> data) {
        Long userId = Long.valueOf(data.get("userId"));
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 校验原密码 (MD5)
        if (!user.getPassword().equals(cn.hutool.crypto.digest.DigestUtil.md5Hex(oldPassword))) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(cn.hutool.crypto.digest.DigestUtil.md5Hex(newPassword));
        userRepository.save(user);
        return Result.success("密码修改成功", null);
    }

    /**
     * 文件上传接口
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("请选择文件");
        }

        try {
            String uploadPath = uploadConfig.getUploadPath();
            File directory = new File(uploadPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            File dest = new File(uploadPath + File.separator + fileName);
            file.transferTo(dest);

            String fileUrl = uploadConfig.getUrlPrefix() + fileName;
            return Result.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("上传失败: " + e.getMessage());
        }
    }
}
