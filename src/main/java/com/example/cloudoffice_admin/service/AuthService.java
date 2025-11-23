package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.dto.*;
import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.repository.UserRepository;
import com.example.cloudoffice_admin.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // 用户登录
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setStatus(User.Status.online);
        userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUser(user);
        return response;
    }

    // 用户注册
    public AuthResponse register(RegisterRequest request) {
        // 验证密码是否匹配
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // 创建新用户
        User user = new User();
        user.setFullName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(User.Status.online);
        user.setAvatarType("emoji"); // 默认使用emoji头像

        userRepository.save(user);

        // 生成认证令牌
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setUser(user);
        return response;
    }

    // 获取当前用户信息
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    // 更新用户信息
    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (request.getName() != null) {
            user.setFullName(request.getName());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getAvatarType() != null) {
            user.setAvatarType(request.getAvatarType());
        }

        return userRepository.save(user);
    }

    // 忘记密码（这里简化实现，实际项目需要发送邮件）
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with this email"));

        // 实际项目中这里应该生成重置令牌并发送邮件
        // 这里只是返回成功消息
        MessageResponse response = new MessageResponse();
        response.setSuccess(true);
        response.setMessage("重置密码邮件已发送");
        return response;
    }

    // 重置密码
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // 实际项目中需要验证重置令牌
        // 这里简化实现，直接根据令牌（假设令牌中包含用户信息）重置密码

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 实际项目中应该从令牌中提取用户信息
        // 这里简化处理
        MessageResponse response = new MessageResponse();
        response.setSuccess(true);
        response.setMessage("密码重置成功");
        return response;
    }
}
