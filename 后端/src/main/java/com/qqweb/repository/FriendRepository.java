package com.qqweb.repository;

import com.qqweb.model.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    /** 查找两个用户之间的好友关系（任意方向） */
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.userId = :a AND f.friendId = :b) OR (f.userId = :b AND f.friendId = :a)")
    Optional<Friend> findBetween(@Param("a") Long a, @Param("b") Long b);

    /** 获取某用户的所有好友（已接受） */
    @Query("SELECT f FROM Friend f WHERE " +
           "(f.userId = :userId OR f.friendId = :userId) AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriends(@Param("userId") Long userId);

    /** 获取发给某用户的好友请求（待处理） */
    List<Friend> findByFriendIdAndStatus(Long friendId, Friend.FriendStatus status);

    /** 获取某用户发出的好友请求（待处理） */
    List<Friend> findByUserIdAndStatus(Long userId, Friend.FriendStatus status);
}
