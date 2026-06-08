package com.qqweb.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器：维护 userId → session 的映射
 */
@Component
public class WebSocketSessionManager {

    // userId → WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 注册会话
     */
    public void register(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    /**
     * 移除会话
     */
    public void remove(String userId) {
        sessions.remove(userId);
    }

    /**
     * 获取会话
     */
    public WebSocketSession get(String userId) {
        return sessions.get(userId);
    }

    /**
     * 用户是否在线
     */
    public boolean isOnline(String userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineCount() {
        int count = 0;
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) count++;
        }
        return count;
    }
}
