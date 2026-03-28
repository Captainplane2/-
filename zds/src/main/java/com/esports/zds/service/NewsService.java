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
     * 获取所有新闻 (支持搜索和排序)
     */
    public List<News> listAllNews(String keyword, String sortBy, String sortOrder) {
        List<News> newsList = newsRepository.findAllByOrderByCreateTimeDesc();
        return filterAndSortNews(newsList, keyword, sortBy, sortOrder);
    }

    /**
     * 根据项目获取新闻
     */
    public List<News> listNewsByProject(String gameProject) {
        return newsRepository.findByGameProjectOrderByCreateTimeDesc(gameProject);
    }

    /**
     * 根据项目获取新闻 (支持搜索和排序)
     */
    public List<News> listNewsByProject(String gameProject, String keyword, String sortBy, String sortOrder) {
        List<News> newsList = newsRepository.findByGameProjectOrderByCreateTimeDesc(gameProject);
        return filterAndSortNews(newsList, keyword, sortBy, sortOrder);
    }

    /**
     * 过滤和排序新闻
     */
    private List<News> filterAndSortNews(List<News> newsList, String keyword, String sortBy, String sortOrder) {
        // 搜索过滤
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            newsList = newsList.stream()
                .filter(news -> news.getTitle().toLowerCase().contains(lowerKeyword) || 
                               news.getContent().toLowerCase().contains(lowerKeyword))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 排序
        if (sortBy != null && !sortBy.isEmpty()) {
            java.util.Comparator<News> comparator;
            switch (sortBy) {
                case "viewCount":
                    comparator = java.util.Comparator.comparingInt(News::getViewCount);
                    break;
                case "createTime":
                default:
                    comparator = java.util.Comparator.comparing(News::getCreateTime);
                    break;
            }
            
            if ("desc".equals(sortOrder)) {
                comparator = comparator.reversed();
            }
            
            newsList.sort(comparator);
        }
        
        return newsList;
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
