package com.example.cloudoffice_admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class WebSocketSessionManager {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 存储用户ID到会话ID的映射
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    // 存储会话ID到用户ID的映射
    private final Map<String, Long> sessionUsers = new ConcurrentHashMap<>();
    // 存储用户在虚拟空间中的会话
    private final Map<Long, Set<Long>> spaceUsers = new ConcurrentHashMap<>();

    // 用户连接时注册会话
    public void registerSession(Long userId, String sessionId) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionUsers.put(sessionId, userId);
    }

    // 用户断开连接时移除会话
    public void removeSession(String sessionId) {
        Long userId = sessionUsers.remove(sessionId);
        if (userId != null) {
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    // 用户所有会话都断开，从所有空间中移除
                    spaceUsers.forEach((spaceId, users) -> users.remove(userId));
                }
            }
        }
    }

    // 用户加入虚拟空间
    public void joinSpace(Long userId, Long spaceId) {
        spaceUsers.computeIfAbsent(spaceId, k -> new CopyOnWriteArraySet<>()).add(userId);
    }

    // 用户离开虚拟空间
    public void leaveSpace(Long userId, Long spaceId) {
        Set<Long> users = spaceUsers.get(spaceId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                spaceUsers.remove(spaceId);
            }
        }
    }

    // 获取虚拟空间中的所有用户
    public Set<Long> getUsersInSpace(Long spaceId) {
        return spaceUsers.getOrDefault(spaceId, new CopyOnWriteArraySet<>());
    }

    // 检查用户是否在线
    public boolean isUserOnline(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    // 向特定用户发送消息
    public void sendToUser(Long userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, payload);
    }

    // 向空间中的所有用户发送消息
    public void broadcastToSpace(Long spaceId, String destination, Object payload) {
        Set<Long> users = getUsersInSpace(spaceId);
        for (Long userId : users) {
            sendToUser(userId, destination, payload);
        }
    }

    // 获取用户的活跃会话数
    public int getUserSessionCount(Long userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }
}
