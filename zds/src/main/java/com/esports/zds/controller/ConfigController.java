package com.esports.zds.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.entity.SysConfig;
import com.esports.zds.repository.SysConfigRepository;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private SysConfigRepository configRepository;

    /**
     * 获取所有配置
     */
    @GetMapping("/list")
    public Result<List<SysConfig>> list() {
        return Result.success(configRepository.findAll());
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody List<SysConfig> configs) {
        configRepository.saveAll(configs);
        return Result.success("配置更新成功", null);
    }
}
