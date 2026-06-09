package com.qqweb.service;

import com.qqweb.model.Friend;
import com.qqweb.model.User;
import com.qqweb.repository.FriendRepository;
import com.qqweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发送好友请求
     */
    public void sendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("不能添加自己为好友");
        }

        Optional<Friend> existing = friendRepository.findBetween(fromUserId, toUserId);
        if (existing.isPresent()) {
            Friend f = existing.get();
            if (f.getStatus() == Friend.FriendStatus.ACCEPTED) {
                throw new BusinessException("已经是好友了");
            }
            if (f.getStatus() == Friend.FriendStatus.PENDING) {
                throw new BusinessException("已发送过好友请求，请等待对方处理");
            }
            // 之前被拒绝过，重新发送
            f.setStatus(Friend.FriendStatus.PENDING);
            f.setUserId(fromUserId);
            f.setFriendId(toUserId);
            friendRepository.save(f);
            return;
        }

        Friend friend = new Friend(fromUserId, toUserId, Friend.FriendStatus.PENDING);
        friendRepository.save(friend);
    }

    /**
     * 接受好友请求
     */
    public void acceptRequest(Long requestId, Long currentUserId) {
        Friend friend = friendRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("请求不存在"));

        if (!friend.getFriendId().equals(currentUserId)) {
            throw new BusinessException("无权操作此请求");
        }
        if (friend.getStatus() != Friend.FriendStatus.PENDING) {
            throw new BusinessException("该请求已处理");
        }

        friend.setStatus(Friend.FriendStatus.ACCEPTED);
        friendRepository.save(friend);
    }

    /**
     * 拒绝好友请求
     */
    public void rejectRequest(Long requestId, Long currentUserId) {
        Friend friend = friendRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("请求不存在"));

        if (!friend.getFriendId().equals(currentUserId)) {
            throw new BusinessException("无权操作此请求");
        }

        friend.setStatus(Friend.FriendStatus.REJECTED);
        friendRepository.save(friend);
    }

    /**
     * 删除好友
     */
    public void deleteFriend(Long currentUserId, Long friendUserId) {
        Friend friend = friendRepository.findBetween(currentUserId, friendUserId)
                .orElseThrow(() -> new BusinessException("还不是好友"));

        if (friend.getStatus() != Friend.FriendStatus.ACCEPTED) {
            throw new BusinessException("还不是好友");
        }

        friendRepository.delete(friend);
    }

    /**
     * 获取好友列表（已接受），返回用户信息
     */
    public List<Map<String, Object>> getFriendList(Long userId) {
        List<Friend> friends = friendRepository.findAcceptedFriends(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Friend f : friends) {
            Long friendUserId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
            User friendUser = userRepository.findById(friendUserId).orElse(null);
            if (friendUser == null) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(friendUser.getId()));
            item.put("username", friendUser.getUsername());
            item.put("nickname", friendUser.getNickname());
            item.put("avatar", friendUser.getAvatar() != null ? friendUser.getAvatar() : "");
            item.put("online", friendUser.getOnline());
            result.add(item);
        }
        return result;
    }

    /**
     * 获取待处理的好友请求
     */
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        List<Friend> pending = friendRepository.findByFriendIdAndStatus(userId, Friend.FriendStatus.PENDING);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Friend f : pending) {
            User fromUser = userRepository.findById(f.getUserId()).orElse(null);
            if (fromUser == null) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", f.getId());
            item.put("userId", String.valueOf(fromUser.getId()));
            item.put("username", fromUser.getUsername());
            item.put("nickname", fromUser.getNickname());
            item.put("avatar", fromUser.getAvatar() != null ? fromUser.getAvatar() : "");
            item.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);
            result.add(item);
        }
        return result;
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) { super(message); }
    }
}
