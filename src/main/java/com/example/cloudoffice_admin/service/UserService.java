package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    User updateUser(Long userId, User userDetails);
    void deleteUser(Long userId);
    Optional<User> getUserById(Long userId);
    List<User> getAllUsers();
    Optional<User> getUserByEmail(String email);
    boolean existsByEmail(String email);
    User updateUserStatus(Long userId, User.Status status);
    List<User> getOnlineUsers();
    boolean isUserActive(Long userId);
}