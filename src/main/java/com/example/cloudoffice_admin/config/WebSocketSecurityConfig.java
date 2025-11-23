package com.example.cloudoffice_admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // 允许所有消息类型的/topic和/queue的订阅
                .nullDestMatcher().authenticated()
                // 允许STOMP连接
                .simpTypeMatchers(SimpMessageType.CONNECT).permitAll()
                // 允许心跳消息
                .simpTypeMatchers(SimpMessageType.HEARTBEAT).permitAll()
                // 允许断开连接消息
                .simpTypeMatchers(SimpMessageType.DISCONNECT).permitAll()
                // 允许发送消息到应用程序目的地
                .simpDestMatchers("/app/**").authenticated()
                // 允许订阅公共主题
                .simpSubscribeDestMatchers("/topic/**").authenticated()
                // 允许订阅个人队列
                .simpSubscribeDestMatchers("/user/**").authenticated()
                // 其他所有消息需要认证
                .anyMessage().authenticated();
    }

    // 不需要CSRF保护，因为WebSocket连接使用不同的机制
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
