package com.qqweb.controller;

import com.qqweb.model.Result;
import com.qqweb.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    private Long getUserId(HttpServletRequest request) {
        return Long.parseLong((String) request.getAttribute("userId"));
    }

    /**
     * 发送好友请求
     * POST /api/friend/request
     * Body: { "toUserId": "2" }
     */
    @PostMapping("/request")
    public Result sendRequest(@RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            Long fromUserId = getUserId(request);
            Long toUserId = Long.parseLong(body.get("toUserId"));
            friendService.sendRequest(fromUserId, toUserId);
            return Result.success("好友请求已发送");
        } catch (FriendService.BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("参数错误");
        }
    }

    /**
     * 获取好友列表
     * GET /api/friend/list
     */
    @GetMapping("/list")
    public Result getFriendList(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(friendService.getFriendList(userId));
    }

    /**
     * 获取待处理的好友请求
     * GET /api/friend/requests
     */
    @GetMapping("/requests")
    public Result getPendingRequests(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(friendService.getPendingRequests(userId));
    }

    /**
     * 接受好友请求
     * POST /api/friend/accept/{id}
     */
    @PostMapping("/accept/{id}")
    public Result acceptRequest(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            friendService.acceptRequest(id, userId);
            return Result.success("已添加为好友");
        } catch (FriendService.BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 拒绝好友请求
     * POST /api/friend/reject/{id}
     */
    @PostMapping("/reject/{id}")
    public Result rejectRequest(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            friendService.rejectRequest(id, userId);
            return Result.success("已拒绝");
        } catch (FriendService.BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除好友
     * DELETE /api/friend/{friendUserId}
     */
    @DeleteMapping("/{friendUserId}")
    public Result deleteFriend(@PathVariable Long friendUserId, HttpServletRequest request) {
        try {
            Long userId = getUserId(request);
            friendService.deleteFriend(userId, friendUserId);
            return Result.success("已删除好友");
        } catch (FriendService.BusinessException e) {
            return Result.error(e.getMessage());
        }
    }
}
