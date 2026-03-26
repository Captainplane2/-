package com.esports.zds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 系统配置实体类 (Key-Value 存储)
 */
@Data
@Entity
@Table(name = "sys_config")
public class SysConfig {

    @Id
    private String configKey; // 配置项键名

    @Column(columnDefinition = "TEXT")
    private String configValue; // 配置项值

    private String remark; // 备注描述
}
