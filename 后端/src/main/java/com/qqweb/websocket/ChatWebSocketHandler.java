package com.qqweb.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqweb.dto.WebSocketMessage;
import com.qqweb.service.ChatService;
import com.qqweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

/**
 * 聊天 WebSocket 处理器
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.register(userId, session);

            // 更新数据库在线状态
            userService.updateOnlineStatus(userId, true);

            // 广播上线通知给所有联系人
            broadcastSystemMessage(userId, "用户 " + userId + " 已上线");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String payload = textMessage.getPayload();

        // 解析消息
        WebSocketMessage msg = objectMapper.readValue(payload, WebSocketMessage.class);

        if ("ping".equals(msg.getType())) {
            // 心跳响应
            sendMessage(session, WebSocketMessage.pong());

        } else if ("chat".equals(msg.getType())) {
            // 处理聊天消息
            handleChatMessage(userId, msg);

        } else if ("typing".equals(msg.getType())) {
            // 正在输入（转发给接收方）
            WebSocketMessage typingMsg = new WebSocketMessage();
            typingMsg.setType("typing");
            typingMsg.setFrom(userId);
            typingMsg.setTo(msg.getTo());
            WebSocketSession targetSession = sessionManager.get(msg.getTo());
            if (targetSession != null && targetSession.isOpen()) {
                sendMessage(targetSession, typingMsg);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.remove(userId);

            // 更新数据库在线状态
            userService.updateOnlineStatus(userId, false);

            // 广播下线通知给所有联系人
            broadcastSystemMessage(userId, "用户 " + userId + " 已下线");
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("[WS] 传输错误: " + exception.getMessage());
    }

    /**
     * 处理聊天消息：持久化 + 转发给接收方 + 回执给发送方
     */
    private void handleChatMessage(String fromUserId, WebSocketMessage msg) throws Exception {
        long timestamp = System.currentTimeMillis();
        String msgId = "msg_" + timestamp + "_" + fromUserId;

        // 1. 持久化到数据库
        chatService.saveMessage(msgId, fromUserId, msg.getTo(),
                msg.getContent(), msg.getContentType(), timestamp);

        // 2. 转发给接收方
        WebSocketMessage chatMsg = WebSocketMessage.chat(
                msgId, fromUserId, msg.getTo(),
                msg.getContent(), msg.getContentType(), timestamp);

        WebSocketSession targetSession = sessionManager.get(msg.getTo());
        if (targetSession != null && targetSession.isOpen()) {
            sendMessage(targetSession, chatMsg);
        }

        // 3. 回执给发送方
        WebSocketMessage ackMsg = WebSocketMessage.ack(msgId, "delivered");
        WebSocketSession senderSession = sessionManager.get(fromUserId);
        if (senderSession != null && senderSession.isOpen()) {
            sendMessage(senderSession, ackMsg);
        }
    }

    /**
     * 广播系统消息（上线/下线）给所有联系人
     */
    private void broadcastSystemMessage(String userId, String content) {
        List<String> contactIds = userService.getContactIds(userId);
        long timestamp = System.currentTimeMillis();
        WebSocketMessage sysMsg = WebSocketMessage.system(userId, content, timestamp);

        for (String contactId : contactIds) {
            WebSocketSession session = sessionManager.get(contactId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, sysMsg);
                } catch (Exception e) {
                    System.err.println("[WS] 广播失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 发送消息到指定会话
     */
    private void sendMessage(WebSocketSession session, Object message) throws Exception {
        String json = objectMapper.writeValueAsString(message);
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        }
    }
}
