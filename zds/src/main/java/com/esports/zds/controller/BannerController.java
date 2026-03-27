package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.entity.Banner;
import com.esports.zds.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerRepository bannerRepository;

    /**
     * 前台获取激活的 Banner (最多6个)
     */
    @GetMapping("/active")
    public Result<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerRepository.findByStatusOrderBySortOrderAsc(0);
        // 限制返回最多6个
        if (banners.size() > 6) {
            banners = banners.subList(0, 6);
        }
        return Result.success(banners);
    }

    /**
     * 后台获取所有 Banner
     */
    @GetMapping("/list")
    public Result<List<Banner>> getAllBanners() {
        return Result.success(bannerRepository.findAll());
    }

    /**
     * 后台保存或更新 Banner
     */
    @PostMapping("/save")
    public Result<Banner> saveBanner(@RequestBody Banner banner) {
        return Result.success(bannerRepository.save(banner));
    }

    /**
     * 后台删除 Banner
     */
    @PostMapping("/delete/{id}")
    public Result<String> deleteBanner(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return Result.success("Banner删除成功", null);
    }
}
