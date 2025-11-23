package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.service.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
// import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Date;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // 未使用到

    @Autowired
    private WebSocketSessionManager sessionManager;

    // 用户连接时注册会话
    @MessageMapping("/connect")
    public void handleConnect(@Payload ConnectMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Long userId = message.getUserId();
        if (userId != null && sessionId != null) {
            sessionManager.registerSession(userId, sessionId);
            // 存储用户ID在会话属性中，便于后续使用
            headerAccessor.getSessionAttributes().put("userId", userId);
        }
    }

    // 更新用户位置信息
    @MessageMapping("/position/update")
    public void updateUserPosition(@Payload PositionUpdateMessage message) {
        message.setTimestamp(new Date());
        // 向特定空间广播位置更新
        sessionManager.broadcastToSpace(message.getSpaceId(), "/queue/positions", message);
    }

    // 用户加入虚拟空间
    @MessageMapping("/space/join")
    public void joinSpace(@Payload SpaceJoinMessage message) {
        message.setTimestamp(new Date());
        sessionManager.joinSpace(message.getUserId(), message.getSpaceId());
        // 通知空间内其他用户
        sessionManager.broadcastToSpace(message.getSpaceId(), "/queue/space/joins", message);
    }

    // 用户离开虚拟空间
    @MessageMapping("/space/leave")
    public void leaveSpace(@Payload SpaceLeaveMessage message) {
        message.setTimestamp(new Date());
        sessionManager.leaveSpace(message.getUserId(), message.getSpaceId());
        // 通知空间内其他用户
        sessionManager.broadcastToSpace(message.getSpaceId(), "/queue/space/leaves", message);
    }

    // 发送私聊消息
    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload PrivateMessage message) {
        message.setTimestamp(new Date());
        // 发送给接收者
        sessionManager.sendToUser(message.getRecipientId(), "/queue/messages", message);
        // 发送给发送者（确认消息已发送）
        sessionManager.sendToUser(message.getSenderId(), "/queue/messages", message);
    }

    // 发送空间公告
    @MessageMapping("/chat/announcement")
    public void sendAnnouncement(@Payload AnnouncementMessage message) {
        message.setTimestamp(new Date());
        sessionManager.broadcastToSpace(message.getSpaceId(), "/queue/chat/announcements", message);
    }

    // 连接消息模型
    public static class ConnectMessage {
        private Long userId;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    // 位置更新消息模型
    public static class PositionUpdateMessage {
        private Long userId;
        private Long spaceId;
        private Long zoneId;
        private int x;
        private int y;
        private Date timestamp;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public Long getZoneId() { return zoneId; }
        public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }

    // 空间加入消息模型
    public static class SpaceJoinMessage {
        private Long userId;
        private Long spaceId;
        private String username;
        private Date timestamp;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }

    // 空间离开消息模型
    public static class SpaceLeaveMessage {
        private Long userId;
        private Long spaceId;
        private String username;
        private Date timestamp;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }

    // 私聊消息模型
    public static class PrivateMessage {
        private Long senderId;
        private String senderName;
        private Long recipientId;
        private String recipientName;
        private String content;
        private Date timestamp;

        // Getters and Setters
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public Long getRecipientId() { return recipientId; }
        public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }

    // 公告消息模型
    public static class AnnouncementMessage {
        private Long senderId;
        private String senderName;
        private Long spaceId;
        private String content;
        private Date timestamp;

        // Getters and Setters
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    }
}
