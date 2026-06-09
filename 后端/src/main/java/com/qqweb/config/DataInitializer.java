package com.qqweb.config;

import com.qqweb.model.Friend;
import com.qqweb.model.User;
import com.qqweb.repository.FriendRepository;
import com.qqweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 数据初始化：管理员账号 + 测试用户 + 好友关系
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Override
    public void run(String... args) {
        // 1. 确保管理员存在
        User admin = ensureUser("admin", "admin123", "管理员", User.Role.ADMIN);

        // 2. 确保测试用户存在
        User zhangsan = ensureUser("zhangsan", "123", "Zhang San", User.Role.USER);
        User lisi = ensureUser("lisi", "123", "Li Si", User.Role.USER);
        User wangwu = ensureUser("wangwu", "123", "Wang Wu", User.Role.USER);

        // 3. 建立好友关系（管理员 ↔ 所有测试用户）
        ensureFriendship(admin.getId(), zhangsan.getId());
        ensureFriendship(admin.getId(), lisi.getId());
        ensureFriendship(admin.getId(), wangwu.getId());

        // 4. 张三和李四互为好友
        ensureFriendship(zhangsan.getId(), lisi.getId());

        System.out.println("[INIT] 数据初始化完成");
    }

    private User ensureUser(String username, String password, String nickname, User.Role role) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            User u = existing.get();
            boolean changed = false;
            if (u.getRole() != role) {
                u.setRole(role);
                changed = true;
            }
            if (!u.getNickname().equals(nickname)) {
                u.setNickname(nickname);
                changed = true;
            }
            if (changed) userRepository.save(u);
            return u;
        }
        User u = new User(username, password, nickname);
        u.setRole(role);
        userRepository.save(u);
        System.out.println("[INIT] 创建用户: " + username);
        return u;
    }

    private void ensureFriendship(Long userId, Long friendId) {
        if (friendRepository.findBetween(userId, friendId).isEmpty()) {
            Friend f = new Friend(userId, friendId, Friend.FriendStatus.ACCEPTED);
            friendRepository.save(f);
            System.out.println("[INIT] 好友关系: " + userId + " <-> " + friendId);
        }
    }
}
