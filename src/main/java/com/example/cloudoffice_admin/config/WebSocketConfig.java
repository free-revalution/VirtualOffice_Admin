package com.example.cloudoffice_admin.config;

import com.example.cloudoffice_admin.security.WebSocketAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的消息代理，用于将消息广播到客户端
        registry.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀，用于发送消息到特定用户
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，客户端将使用此端点连接到服务器
        // 使用ApplicationContext获取依赖，打破循环依赖
        WebSocketAuthenticationInterceptor webSocketAuthenticationInterceptor = 
            applicationContext.getBean(WebSocketAuthenticationInterceptor.class);
            
        registry.addEndpoint("/ws")
                .addInterceptors(webSocketAuthenticationInterceptor)
                .setAllowedOrigins("*")
                .setHandshakeHandler(new DefaultHandshakeHandler())
                .withSockJS(); // 启用SockJS协议，提供备选传输方式
    }
}
