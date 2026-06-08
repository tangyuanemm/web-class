package com.qqweb.service;

import com.qqweb.model.Message;
import com.qqweb.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * 保存消息
     */
    public Message saveMessage(String msgId, String from, String to,
                                String content, String contentType, Long timestamp) {
        Message msg = new Message(msgId, from, to, content, contentType, timestamp);
        return messageRepository.save(msg);
    }

    /**
     * 获取聊天记录（分页）
     */
    public Map<String, Object> getHistory(String userIdA, String userIdB, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Message> messagePage = messageRepository.findChatHistory(userIdA, userIdB, pageable);

        // 转换为前端要求的格式，按时间正序
        List<Message> list = messagePage.getContent();
        list.sort(Comparator.comparing(Message::getTimestamp));

        List<Map<String, Object>> msgList = new ArrayList<>();
        for (Message m : list) {
            Map<String, Object> msgMap = new LinkedHashMap<>();
            msgMap.put("msgId", m.getMsgId());
            msgMap.put("from", m.getFromUser());
            msgMap.put("to", m.getToUser());
            msgMap.put("content", m.getContent());
            msgMap.put("contentType", m.getContentType());
            msgMap.put("timestamp", m.getTimestamp());
            msgList.add(msgMap);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", messagePage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("list", msgList);
        return result;
    }

    /**
     * 统计用户数据
     */
    public Map<String, Object> getStatistics(String userId) {
        // 今天零点的时间戳
        long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli();

        long totalMessages = messageRepository.countByUser(userId);
        long todayMessages = messageRepository.countTodayByUser(userId, todayStart);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalMessages", totalMessages);
        stats.put("todayMessages", todayMessages);
        return stats;
    }
}
