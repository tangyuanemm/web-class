package com.qqweb.websocket;

import com.qqweb.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器：从 URL 参数中获取 token 并验证
 */
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            String token = servletRequest.getServletRequest().getParameter("token");
            String userId = servletRequest.getServletRequest().getParameter("userId");

            if (token == null || userId == null) {
                return false;
            }

            // 验证 token
            if (!jwtUtil.validateToken(token)) {
                return false;
            }

            // 验证 token 中的 userId 与参数 userId 一致
            String tokenUserId = jwtUtil.getUserIdFromToken(token);
            if (!userId.equals(tokenUserId)) {
                return false;
            }

            // 将 userId 存入 WebSocket session attributes
            attributes.put("userId", userId);
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
