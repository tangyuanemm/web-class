package com.qqweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "msg_id", nullable = false, unique = true, length = 50)
    private String msgId;

    @Column(name = "from_user", nullable = false, length = 50)
    private String fromUser;

    @Column(name = "to_user", nullable = false, length = 50)
    private String toUser;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_type", nullable = false, length = 20)
    private String contentType = "text";

    @Column(nullable = false)
    private Long timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Message() {}

    public Message(String msgId, String fromUser, String toUser,
                   String content, String contentType, Long timestamp) {
        this.msgId = msgId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.contentType = contentType;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }

    public String getFromUser() { return fromUser; }
    public void setFromUser(String fromUser) { this.fromUser = fromUser; }

    public String getToUser() { return toUser; }
    public void setToUser(String toUser) { this.toUser = toUser; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
