package com.qqweb.controller;

import com.qqweb.model.Result;
import com.qqweb.model.User;
import com.qqweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    /**
     * 验证是否为管理员
     */
    private User checkAdmin(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User admin = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("无管理员权限");
        }
        return admin;
    }

    /**
     * 获取所有用户列表
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public Result getUsers(HttpServletRequest request) {
        try {
            checkAdmin(request);
            List<User> all = userRepository.findAll();
            List<Map<String, Object>> list = new ArrayList<>();
            for (User u : all) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", String.valueOf(u.getId()));
                item.put("username", u.getUsername());
                item.put("nickname", u.getNickname());
                item.put("role", u.getRole().name().toLowerCase());
                item.put("status", u.getStatus().name().toLowerCase());
                item.put("online", u.getOnline());
                item.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
                list.add(item);
            }
            return Result.success(list);
        } catch (RuntimeException e) {
            return Result.error(403, e.getMessage());
        }
    }

    /**
     * 删除用户
     * DELETE /api/admin/users/{username}
     */
    @DeleteMapping("/users/{username}")
    public Result deleteUser(@PathVariable String username, HttpServletRequest request) {
        try {
            checkAdmin(request);
            if ("admin".equals(username)) {
                return Result.error("不能删除管理员账号");
            }
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return Result.error("用户不存在");
            }
            userRepository.delete(user);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(403, e.getMessage());
        }
    }

    /**
     * 重置用户密码
     * PUT /api/admin/users/{username}/password
     */
    @PutMapping("/users/{username}/password")
    public Result resetPassword(@PathVariable String username,
                                 @RequestBody Map<String, String> body,
                                 HttpServletRequest request) {
        try {
            checkAdmin(request);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                return Result.error("用户不存在");
            }
            String newPassword = body.get("password");
            if (newPassword == null || newPassword.length() < 3) {
                return Result.error("密码至少3位");
            }
            user.setPassword(newPassword);
            userRepository.save(user);
            return Result.success("密码已重置");
        } catch (RuntimeException e) {
            return Result.error(403, e.getMessage());
        }
    }
}
