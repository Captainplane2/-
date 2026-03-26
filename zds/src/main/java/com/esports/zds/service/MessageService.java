package com.esports.zds.service;

import java.util.List;

import com.esports.zds.entity.Message;

/**
 * 留言业务接口
 */
public interface MessageService {
    void submit(Message message);
    List<Message> listByUserId(Long userId);
    List<Message> listAllPending();
    void reply(Long id, String content);
}
