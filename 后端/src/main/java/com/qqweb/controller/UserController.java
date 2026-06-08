package com.qqweb.controller;

import com.qqweb.dto.LoginRequest;
import com.qqweb.dto.RegisterRequest;
import com.qqweb.model.Result;
import com.qqweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest req) {
        try {
            userService.register(req);
            return Result.success("注册成功", null);
        } catch (UserService.BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest req) {
        try {
            Map<String, Object> data = userService.login(req);
            return Result.success("登录成功", data);
        } catch (UserService.BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取联系人列表
     * GET /api/user/contacts
     */
    @GetMapping("/contacts")
    public Result getContacts(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<Map<String, Object>> contacts = userService.getContacts(userId);
        return Result.success(contacts);
    }

    /**
     * 搜索用户
     * GET /api/user/search?keyword=xxx
     */
    @GetMapping("/search")
    public Result searchUser(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        List<Map<String, Object>> users = userService.searchUser(keyword.trim());
        return Result.success(users);
    }
}
