package com.qqweb.repository;

import com.qqweb.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查询两个用户之间的聊天记录（分页，按时间倒序）
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.fromUser = :userA AND m.toUser = :userB) OR " +
           "(m.fromUser = :userB AND m.toUser = :userA) " +
           "ORDER BY m.timestamp DESC")
    Page<Message> findChatHistory(String userA, String userB, Pageable pageable);

    /**
     * 根据 msgId 查找消息
     */
    Optional<Message> findByMsgId(String msgId);

    /**
     * 统计某用户的总消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.fromUser = :userId OR m.toUser = :userId")
    long countByUser(String userId);

    /**
     * 统计今天的消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "(m.fromUser = :userId OR m.toUser = :userId) " +
           "AND m.timestamp >= :todayStart")
    long countTodayByUser(String userId, long todayStart);
}
