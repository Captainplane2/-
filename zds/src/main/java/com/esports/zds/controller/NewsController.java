package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.entity.News;
import com.esports.zds.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电竞新闻接口控制器
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    /**
     * 保存或更新新闻 (管理端使用)
     */
    @PostMapping("/save")
    public Result<News> saveNews(@RequestBody News news) {
        News savedNews = newsService.saveNews(news);
        return Result.success("新闻发布成功", savedNews);
    }

    /**
     * 获取新闻列表
     * @param gameProject 可选，按游戏项目筛选
     */
    @GetMapping("/list")
    public Result<List<News>> listNews(@RequestParam(required = false) String gameProject) {
        List<News> list;
        if (gameProject != null && !gameProject.isEmpty() && !"全部".equals(gameProject)) {
            list = newsService.listNewsByProject(gameProject);
        } else {
            list = newsService.listAllNews();
        }
        return Result.success(list);
    }

    /**
     * 获取单条新闻详情
     */
    @GetMapping("/{id}")
    public Result<News> getNews(@PathVariable Long id) {
        News news = newsService.getById(id);
        if (news != null) {
            return Result.success(news);
        } else {
            return Result.error(404, "新闻不存在");
        }
    }

    /**
     * 删除新闻 (管理端使用)
     */
    @PostMapping("/delete/{id}")
    public Result<String> deleteNews(@PathVariable Long id) {
        newsService.deleteNews(id);
        return Result.success("新闻删除成功", null);
    }
}
