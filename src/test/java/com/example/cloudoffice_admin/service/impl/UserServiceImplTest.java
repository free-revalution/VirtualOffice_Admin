package com.example.cloudoffice_admin.service.impl;

import com.example.cloudoffice_admin.TestUtils;
import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestUtils.createTestUser(1L);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {
        // 准备
        User userToCreate = new User();
        userToCreate.setEmail("newuser@example.com");
        userToCreate.setPassword("Password@123");
        userToCreate.setUsername("newuser");
        userToCreate.setFullName("New User");
        userToCreate.setRole("USER");

        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);

        // 执行
        User createdUser = userService.createUser(userToCreate);

        // 验证
        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals("INACTIVE", createdUser.getStatus());
        verify(passwordEncoder).encode("Password@123");
        verify(userRepository).save(any(User.class));
        verify(userRepository).existsByEmail("newuser@example.com");
    }

    @Test
    void createUser_withExistingEmail_shouldThrowException() {
        // 准备
        User userToCreate = new User();
        userToCreate.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userToCreate));
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUser() {
        // 准备
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行
        Optional<User> foundUser = userService.getUserById(1L);

        // 验证
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_withNonExistentId_shouldReturnEmpty() {
        // 准备
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行
        Optional<User> foundUser = userService.getUserById(999L);

        // 验证
        assertFalse(foundUser.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // 准备
        List<User> expectedUsers = Arrays.asList(
                TestUtils.createTestUser(1L),
                TestUtils.createTestUser(2L)
        );
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // 执行
        List<User> users = userService.getAllUsers();

        // 验证
        assertNotNull(users);
        assertEquals(2, users.size());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_shouldUpdateUserSuccessfully() {
        // 准备
        User updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setRole("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行
        User result = userService.updateUser(1L, updatedUser);

        // 验证
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_withNonExistentId_shouldThrowException() {
        // 准备
        User updatedUser = new User();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(999L, updatedUser));
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        // 准备
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行
        userService.deleteUser(1L);

        // 验证
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_withNonExistentId_shouldThrowException() {
        // 准备
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行和验证
        assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });

        // 验证
        verify(userRepository).findById(999L);
    }

    @Test
    void updateUserStatus_shouldUpdateStatus() {
        // 准备
        User.Status newStatus = User.Status.offline;
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // 执行
        User result = userService.updateUserStatus(1L, newStatus);

        // 验证
        assertNotNull(result);
        assertEquals(newStatus, result.getStatusEnum());
        verify(userRepository).findById(1L);
        verify(userRepository).save(testUser);
    }

    @Test
    void isUserActive_shouldReturnTrueIfActive() {
        // 准备
        testUser.setStatus("online"); // 设置为在线状态
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行
        boolean isActive = userService.isUserActive(1L);

        // 验证
        assertTrue(isActive);
        verify(userRepository).findById(1L);
    }

    @Test
    void isUserActive_shouldReturnFalseIfInactive() {
        // 准备
        testUser.setStatus("offline"); // 设置为离线状态
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // 执行
        boolean isActive = userService.isUserActive(1L);

        // 验证
        assertFalse(isActive);
        verify(userRepository).findById(1L);
    }
}
