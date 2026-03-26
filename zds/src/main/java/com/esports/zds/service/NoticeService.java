package com.esports.zds.service;

import java.util.List;

import com.esports.zds.entity.Notice;

/**
 * 公告业务接口
 */
public interface NoticeService {
    List<Notice> listActiveNotices();
    Notice getById(Long id);
    void save(Notice notice);
    void delete(Long id);
}
