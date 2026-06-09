package com.qqweb.config;

import com.qqweb.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Web MVC 配置：JWT 拦截器 + 静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.qqweb.service.FileService fileService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                     Object handler) throws Exception {
                // OPTIONS 预检请求放行
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    return true;
                }

                String path = request.getRequestURI();
                // 登录、注册接口不需要认证
                if (path.startsWith("/api/user/login") || path.startsWith("/api/user/register")) {
                    return true;
                }

                // 获取 Token
                String authHeader = request.getHeader("Authorization");

                // 兼容 HTTP 和 WebSocket 升级请求（/chat 路径由 WebSocket 拦截器处理）
                if (path.startsWith("/chat")) {
                    return true;
                }

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"msg\":\"未登录\",\"data\":null}");
                    return false;
                }

                String token = authHeader.substring(7);
                if (!jwtUtil.validateToken(token)) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\",\"data\":null}");
                    return false;
                }

                // 将 userId 存入 request attribute
                String userId = jwtUtil.getUserIdFromToken(token);
                request.setAttribute("userId", userId);
                return true;
            }
        }).addPathPatterns("/api/**")
          .excludePathPatterns("/api/user/login", "/api/user/register");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件目录为可访问的 URL（使用绝对路径）
        // Spring 会把 /uploads/xxx 映射到 resourceLocation/xxx
        // uploadPath 是 .../uploads/images，所以要用它的父目录 .../uploads
        String uploadPath = fileService.getAbsoluteUploadPath();
        String parentPath = java.nio.file.Paths.get(uploadPath).getParent().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + parentPath + "/");
    }
}
