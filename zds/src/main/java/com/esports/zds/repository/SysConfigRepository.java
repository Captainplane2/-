package com.esports.zds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esports.zds.entity.SysConfig;

/**
 * 系统配置数据访问接口
 */
@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, String> {
}
