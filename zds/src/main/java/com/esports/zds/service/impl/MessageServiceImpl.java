package com.esports.zds.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.esports.zds.entity.Message;
import com.esports.zds.repository.MessageRepository;
import com.esports.zds.service.MessageService;

/**
 * 留言业务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public void submit(Message message) {
        message.setStatus(0);
        messageRepository.save(message);
    }

    @Override
    public List<Message> listByUserId(Long userId) {
        return messageRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    @Override
    public List<Message> listAllPending() {
        return messageRepository.findByStatus(0);
    }

    @Override
    public void reply(Long id, String content) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("留言不存在"));
        message.setReply(content);
        message.setReplyTime(LocalDateTime.now());
        message.setStatus(1);
        messageRepository.save(message);
    }
}
