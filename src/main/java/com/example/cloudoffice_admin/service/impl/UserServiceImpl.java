package com.example.cloudoffice_admin.service.impl;

import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.repository.UserRepository;
import com.example.cloudoffice_admin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        // 检查邮箱是否已存在
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("USER"); // 使用大写角色名称
        }
        
        // 设置创建时间和默认状态
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        
        // 明确设置默认状态为非活跃
        user.setStatus("INACTIVE");
        // 确保userStatus字段正确设置
        try {
            java.lang.reflect.Field field = user.getClass().getDeclaredField("userStatus");
            field.setAccessible(true);
            field.set(user, "INACTIVE");
        } catch (Exception e) {
            // 忽略反射异常，继续执行
        }
        
        // 为每个用户生成唯一的用户ID
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            user.setUserId("user_" + System.currentTimeMillis() + "_" + user.getEmail().hashCode());
        }
        
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // 更新用户信息，但不更新密码（除非明确提供）
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }
        if (userDetails.getFullName() != null) {
            user.setFullName(userDetails.getFullName());
        }
        
        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(user.getEmail())) {
            if (existsByEmail(userDetails.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(userDetails.getEmail());
        }
        
        if (userDetails.getAvatar() != null) {
            user.setAvatar(userDetails.getAvatar());
        }
        
        if (userDetails.getAvatarType() != null) {
            user.setAvatarType(userDetails.getAvatarType());
        }
        
        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        // 添加状态更新支持
        if (userDetails.getStatus() != null) {
            user.setStatus(userDetails.getStatus());
        }
        
        // 更新最后活动时间
        user.setLastActive(LocalDateTime.now());
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // 确保删除前用户存在
        userRepository.delete(user);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User updateUserStatus(Long userId, User.Status status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        user.setStatus(status);
        
        // 如果状态为在线，更新最后活动时间
        if (User.Status.online.equals(status)) {
            user.setLastActive(LocalDateTime.now());
        }
        
        return userRepository.save(user);
    }

    @Override
    public List<User> getOnlineUsers() {
        // 优化查询，避免全部加载后过滤
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> "online".equals(user.getStatus()) || 
                               "away".equals(user.getStatus()) || 
                               "busy".equals(user.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUserActive(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.isPresent() && 
               ("online".equals(userOptional.get().getStatus()) || 
                "away".equals(userOptional.get().getStatus()) || 
                "busy".equals(userOptional.get().getStatus()));
    }
    
    // 新增方法：更新用户最后活动时间
    @Override
    public void updateLastActiveTime(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }
    
    // 新增方法：按角色获取用户
    @Override
    public List<User> getUsersByRole(String role) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> role.equals(user.getRole()))
                .collect(Collectors.toList());
    }
}