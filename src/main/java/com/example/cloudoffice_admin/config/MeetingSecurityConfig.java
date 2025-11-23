package com.example.cloudoffice_admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MeetingSecurityConfig {

    @Bean
    public SecurityFilterChain meetingSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // 配置会议相关的安全规则
            .securityMatcher("/api/meetings/**")
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authorize -> authorize
                // 允许公开访问的端点
                .requestMatchers("/api/meetings/rooms/check-name").permitAll()
                // WebSocket端点需要特定的安全配置
                .requestMatchers("/ws/**").permitAll()
                // 所有其他会议相关端点都需要认证
                .anyRequest().authenticated()
            );
            
        // 注意：由于JwtAuthenticationFilter尚未实现，暂时不添加此过滤器
        // 如果需要JWT认证，可以在实现JwtAuthenticationFilter后添加
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
