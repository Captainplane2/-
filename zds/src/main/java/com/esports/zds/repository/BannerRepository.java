package com.esports.zds.repository;

import com.esports.zds.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    // 按照状态和排序权重查找 (0 为启用，按 sortOrder 升序)
    List<Banner> findByStatusOrderBySortOrderAsc(Integer status);
}
