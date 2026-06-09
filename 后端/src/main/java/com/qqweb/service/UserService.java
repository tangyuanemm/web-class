package com.qqweb.service;

import com.qqweb.dto.LoginRequest;
import com.qqweb.dto.RegisterRequest;
import com.qqweb.model.User;
import com.qqweb.repository.UserRepository;
import com.qqweb.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FriendService friendService;

    /**
     * 注册
     */
    public Map<String, Object> register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User(req.getUsername(), req.getPassword(), req.getNickname());
        // TODO: 实际项目中应对密码进行 BCrypt 加密
        // user.setPassword(new BCryptPasswordEncoder().encode(req.getPassword()));
        userRepository.save(user);

        return null;
    }

    /**
     * 登录
     */
    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        // 检查账号是否被禁用
        if (user.getStatus() == User.Status.DISABLED) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!user.getPassword().equals(req.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String userId = String.valueOf(user.getId());
        String token = jwtUtil.generateToken(userId, user.getUsername());

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", userId);
        userMap.put("username", user.getUsername());
        userMap.put("nickname", user.getNickname());
        userMap.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        userMap.put("role", user.getRole().name().toLowerCase());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", userMap);
        return result;
    }

    /**
     * 获取联系人列表（只返回已添加的好友）
     */
    public List<Map<String, Object>> getContacts(String currentUserId) {
        Long myId = Long.parseLong(currentUserId);
        return friendService.getFriendList(myId);
    }

    /**
     * 搜索用户
     */
    public List<Map<String, Object>> searchUser(String keyword) {
        List<User> users = userRepository.searchByKeyword(keyword);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", String.valueOf(u.getId()));
            userMap.put("username", u.getUsername());
            userMap.put("nickname", u.getNickname());
            userMap.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
            result.add(userMap);
        }
        return result;
    }

    /**
     * 通过 ID 查找用户
     */
    public User findById(String userId) {
        return userRepository.findById(Long.parseLong(userId)).orElse(null);
    }

    /**
     * 更新在线状态
     */
    public void updateOnlineStatus(String userId, boolean online) {
        User user = findById(userId);
        if (user != null) {
            user.setOnline(online);
            userRepository.save(user);
        }
    }

    /**
     * 获取用户的所有联系人 ID（所有启用用户，排除自己）
     */
    public List<String> getContactIds(String userId) {
        Long myId = Long.parseLong(userId);
        List<User> users = userRepository.findByStatus(User.Status.ENABLED);
        List<String> ids = new ArrayList<>();
        for (User u : users) {
            if (!u.getId().equals(myId)) {
                ids.add(String.valueOf(u.getId()));
            }
        }
        return ids;
    }

    /**
     * 业务异常
     */
    public static class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
