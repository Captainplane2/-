package com.esports.zds.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esports.zds.entity.Notice;
import com.esports.zds.repository.NoticeRepository;
import com.esports.zds.service.NoticeService;

/**
 * 公告业务实现类
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Override
    public List<Notice> listActiveNotices() {
        return noticeRepository.findByStatusOrderByCreateTimeDesc(1);
    }

    @Override
    public Notice getById(Long id) {
        Notice notice = noticeRepository.findById(id).orElse(null);
        if (notice != null) {
            notice.setViews(notice.getViews() + 1);
            noticeRepository.save(notice);
        }
        return notice;
    }

    @Override
    public void save(Notice notice) {
        noticeRepository.save(notice);
    }

    @Override
    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }
}
