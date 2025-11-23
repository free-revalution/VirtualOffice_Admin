package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 获取所有用户（仅管理员）
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 获取在线用户
    @GetMapping("/online")
    public ResponseEntity<List<User>> getOnlineUsers() {
        List<User> onlineUsers = userService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    // 获取单个用户信息
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return ResponseEntity.ok(user);
    }

    // 创建新用户（仅管理员）
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(201).body(createdUser);
    }

    // 更新用户信息
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(userId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    // 更新用户状态
    @PutMapping("/{userId}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam User.Status status) {
        User updatedUser = userService.updateUserStatus(userId, status);
        return ResponseEntity.ok(updatedUser);
    }

    // 删除用户（仅管理员）
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // 检查用户是否活跃
    @GetMapping("/{userId}/active")
    public ResponseEntity<Boolean> isUserActive(@PathVariable Long userId) {
        boolean isActive = userService.isUserActive(userId);
        return ResponseEntity.ok(isActive);
    }

    // 获取当前登录用户信息
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Long currentUserId = getCurrentUserId();
        User user = userService.getUserById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
        return ResponseEntity.ok(user);
    }

    // 更新当前用户信息
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody User userDetails) {
        Long currentUserId = getCurrentUserId();
        // 不允许普通用户更改自己的角色
        if (userDetails.getRole() != null) {
            User currentUser = userService.getUserById(currentUserId).orElseThrow();
            if (!"admin".equals(currentUser.getRole())) {
                userDetails.setRole(null); // 清除角色字段，防止非管理员更改角色
            }
        }
        User updatedUser = userService.updateUser(currentUserId, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    // 获取当前登录用户的ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal() == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalArgumentException("User not authenticated");
        }
        // 假设authentication.getName()返回用户ID
        return Long.valueOf(authentication.getName());
    }
}