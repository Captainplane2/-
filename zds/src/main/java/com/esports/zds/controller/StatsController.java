package com.esports.zds.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esports.zds.common.Result;
import com.esports.zds.repository.MatchInfoRepository;
import com.esports.zds.repository.PostRepository;
import com.esports.zds.repository.TeamRepository;
import com.esports.zds.repository.UserRepository;

/**
 * 后台统计模块控制器
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchInfoRepository matchInfoRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * 获取后台首页统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 1. 基础计数值
        stats.put("userCount", userRepository.count());
        stats.put("teamCount", teamRepository.count());
        stats.put("matchCount", matchInfoRepository.count());
        stats.put("postCount", postRepository.count());

        // 2. 性别分布 (简单模拟，实际可从数据库聚合查询)
        Map<String, Long> genderData = new HashMap<>();
        genderData.put("男", userRepository.count()); // 示例数据
        genderData.put("女", 0L);
        stats.put("genderDist", genderData);

        // 3. 游戏项目分布
        // 这里仅作为示例，实际开发中建议使用 @Query 进行聚合
        stats.put("gameDist", teamRepository.findAll().stream()
                .map(t -> t.getGameProject())
                .filter(g -> g != null)
                .collect(java.util.stream.Collectors.groupingBy(g -> g, java.util.stream.Collectors.counting())));

        return Result.success(stats);
    }
}
