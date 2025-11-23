package com.example.cloudoffice_admin.security;

import com.example.cloudoffice_admin.repository.UserRepository;
import com.example.cloudoffice_admin.service.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从请求头或查询参数中获取JWT令牌
        String token = null;
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 尝试从请求头获取
            token = servletRequest.getHeader("Authorization");
            // 如果请求头中没有，尝试从查询参数获取
            if (token == null) {
                token = servletRequest.getParameter("token");
            }
        }

        // 如果找到了令牌，验证并提取用户信息
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 移除"Bearer "前缀
            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromJWT(token);
                // 从数据库获取用户信息
                userRepository.findByEmail(username)
                        .ifPresent(user -> {
                            // 将用户ID存储在WebSocket会话属性中
                            attributes.put("userId", user.getId().toString());
                        });
            }
        }

        // 即使没有认证也允许握手，但会在后续消息处理中进行权限检查
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理
    }

    // WebSocket断开连接处理应该在WebSocketHandler实现中处理，而非HandshakeHandler
    // 这个内部类暂时移除，避免编译错误
}
