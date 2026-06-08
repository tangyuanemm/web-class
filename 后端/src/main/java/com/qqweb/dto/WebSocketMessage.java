package com.qqweb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * WebSocket 消息体
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {
    private String type;        // chat, ping, pong, system, ack, typing
    private String msgId;
    private String from;
    private String to;
    private String content;
    private String contentType; // text, emoji, image
    private Long timestamp;
    private String userId;      // 用于 system 消息
    private String status;      // 用于 ack 消息：delivered

    public WebSocketMessage() {}

    // 工厂方法：创建聊天消息
    public static WebSocketMessage chat(String msgId, String from, String to,
                                         String content, String contentType, Long timestamp) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.type = "chat";
        msg.msgId = msgId;
        msg.from = from;
        msg.to = to;
        msg.content = content;
        msg.contentType = contentType;
        msg.timestamp = timestamp;
        return msg;
    }

    // 工厂方法：创建回执
    public static WebSocketMessage ack(String msgId, String status) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.type = "ack";
        msg.msgId = msgId;
        msg.status = status;
        return msg;
    }

    // 工厂方法：创建系统通知
    public static WebSocketMessage system(String userId, String content, Long timestamp) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.type = "system";
        msg.userId = userId;
        msg.content = content;
        msg.timestamp = timestamp;
        return msg;
    }

    // 工厂方法：创建心跳响应
    public static WebSocketMessage pong() {
        WebSocketMessage msg = new WebSocketMessage();
        msg.type = "pong";
        return msg;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
