package com.qqweb.controller;

import com.qqweb.model.Result;
import com.qqweb.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 获取历史消息
     * GET /api/chat/history?with=user_002&page=1&size=50
     */
    @GetMapping("/history")
    public Result getHistory(@RequestParam("with") String withUserId,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "50") int size,
                             HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");

        if (withUserId == null || withUserId.trim().isEmpty()) {
            return Result.error("参数 with 不能为空");
        }

        Map<String, Object> data = chatService.getHistory(userId, withUserId.trim(), page, size);
        return Result.success(data);
    }
}
