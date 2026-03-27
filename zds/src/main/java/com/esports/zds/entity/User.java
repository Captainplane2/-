package com.esports.zds.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // 账号

    @Column(nullable = false)
    private String password; // 密码

    private String nickname; // 昵称
    
    private String realName; // 真实姓名
    
    private String gender;   // 性别
    
    private String avatar;   // 头像

    private String university; // 所属高校
    
    private String studentId;  // 学号
    
    private String phone;      // 联系电话
    
    private String qq;         // QQ号
    
    private String wechat;     // 微信号
    
    private String signature;  // 个性签名

    /**
     * 角色：ROLE_USER (普通用户), ROLE_TEAM_MEMBER (战队队员), ROLE_LEADER (战队队长), ROLE_ADMIN (管理员)
     */
    private String role;

    /**
     * 认证状态：0 未认证, 1 已认证
     */
    private Integer status = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
