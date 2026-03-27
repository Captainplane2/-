package com.esports.zds.service;

import com.esports.zds.entity.News;
import com.esports.zds.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 电竞新闻业务逻辑类
 */
@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    /**
     * 保存或更新新闻
     */
    public News saveNews(News news) {
        return newsRepository.save(news);
    }

    /**
     * 获取所有新闻 (按时间倒序)
     */
    public List<News> listAllNews() {
        return newsRepository.findAllByOrderByCreateTimeDesc();
    }

    /**
     * 根据项目获取新闻
     */
    public List<News> listNewsByProject(String gameProject) {
        return newsRepository.findByGameProjectOrderByCreateTimeDesc(gameProject);
    }

    /**
     * 根据ID获取新闻详情
     */
    public News getById(Long id) {
        News news = newsRepository.findById(id).orElse(null);
        if (news != null) {
            // 增加浏览量逻辑
            news.setViewCount(news.getViewCount() + 1);
            newsRepository.save(news);
        }
        return news;
    }

    /**
     * 删除新闻
     */
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }
}
