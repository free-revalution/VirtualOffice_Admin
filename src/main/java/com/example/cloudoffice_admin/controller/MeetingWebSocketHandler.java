package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.model.MeetingParticipant;
// import com.example.cloudoffice_admin.model.MeetingSession;
import com.example.cloudoffice_admin.service.MeetingService;
import com.example.cloudoffice_admin.service.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class MeetingWebSocketHandler {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private WebSocketSessionManager sessionManager;

    // 处理用户连接到会议
    @MessageMapping("/meetings/{sessionId}/connect")
    @SendTo("/topic/meetings/{sessionId}")
    public Map<String, Object> handleConnect(@DestinationVariable Long sessionId, 
                                           @Headers Map<String, Object> headers,
                                           Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        String sessionIdHeader = headers.get("simpSessionId").toString();

        // 注册WebSocket会话
        sessionManager.registerSession(userId, sessionIdHeader);

        // 返回连接确认信息
        return Map.of(
            "event", "USER_CONNECTED",
            "userId", userId,
            "sessionId", sessionId,
            "message", "Successfully connected to meeting session"
        );
    }

    // 处理用户加入会议
    @MessageMapping("/meetings/{sessionId}/join")
    @SendTo("/topic/meetings/{sessionId}")
    public MeetingParticipant handleJoin(@DestinationVariable Long sessionId, 
                                        @Payload Map<String, Object> payload,
                                        Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // 创建参与者请求
        com.example.cloudoffice_admin.dto.ParticipantRequest request = new com.example.cloudoffice_admin.dto.ParticipantRequest();
        request.setUserId(userId);
        request.setDisplayName((String) payload.getOrDefault("displayName", null));
        request.setRole((String) payload.getOrDefault("role", "participant"));
        request.setAudioOn((boolean) payload.getOrDefault("audioOn", true));
        request.setVideoOn((boolean) payload.getOrDefault("videoOn", false));

        // 调用服务加入会议
        return meetingService.joinMeetingSession(sessionId, request);
    }

    // 处理用户离开会议
    @MessageMapping("/meetings/{sessionId}/leave")
    @SendTo("/topic/meetings/{sessionId}")
    public Map<String, Object> handleLeave(@DestinationVariable Long sessionId,
                                          Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        // 查找用户在会话中的参与者记录
        com.example.cloudoffice_admin.model.MeetingParticipant participant = 
            meetingService.getSessionParticipants(sessionId).stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        // 调用服务离开会议
        meetingService.leaveMeetingSession(participant.getId());
        
        return Map.of(
            "event", "USER_LEFT",
            "userId", userId,
            "sessionId", sessionId
        );
    }

    // 处理参与者状态更新（麦克风/摄像头）
    @MessageMapping("/meetings/{sessionId}/participant/status")
    @SendTo("/topic/meetings/{sessionId}")
    public MeetingParticipant handleParticipantStatus(@DestinationVariable Long sessionId,
                                                    @Payload Map<String, Object> payload,
                                                    Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        boolean audioOn = (boolean) payload.getOrDefault("audioOn", false);
        boolean videoOn = (boolean) payload.getOrDefault("videoOn", false);
        
        // 查找用户在会话中的参与者记录
        com.example.cloudoffice_admin.model.MeetingParticipant participant = 
            meetingService.getSessionParticipants(sessionId).stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        // 更新状态
        return meetingService.updateParticipantStatus(participant.getId(), audioOn, videoOn);
    }

    // 处理屏幕共享状态更新
    @MessageMapping("/meetings/{sessionId}/screen-sharing")
    @SendTo("/topic/meetings/{sessionId}")
    public Map<String, Object> handleScreenSharing(@DestinationVariable Long sessionId,
                                                 @Payload Map<String, Object> payload,
                                                 Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        boolean isSharing = (boolean) payload.getOrDefault("isSharing", false);
        
        // 这里可以更新数据库中的屏幕共享状态
        // 简化版只发送广播消息
        return Map.of(
            "event", "SCREEN_SHARING_STATUS",
            "userId", userId,
            "isSharing", isSharing
        );
    }

    // 处理聊天消息
    @MessageMapping("/meetings/{sessionId}/chat")
    @SendTo("/topic/meetings/{sessionId}/chat")
    public Map<String, Object> handleChat(@DestinationVariable Long sessionId,
                                        @Payload Map<String, Object> payload,
                                        Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        String message = (String) payload.get("message");
        
        // 返回聊天消息
        return Map.of(
            "event", "CHAT_MESSAGE",
            "userId", userId,
            "username", authentication.getName(),
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
    }

    // 处理举手/放下手
    @MessageMapping("/meetings/{sessionId}/hand-raise")
    @SendTo("/topic/meetings/{sessionId}")
    public Map<String, Object> handleHandRaise(@DestinationVariable Long sessionId,
                                             @Payload Map<String, Object> payload,
                                             Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        boolean isRaised = (boolean) payload.getOrDefault("isRaised", false);
        
        // 简化版只发送广播消息
        return Map.of(
            "event", "HAND_RAISE",
            "userId", userId,
            "isRaised", isRaised
        );
    }

    // 处理主持人操作
    @MessageMapping("/meetings/{sessionId}/host-action")
    @SendTo("/topic/meetings/{sessionId}")
    public Map<String, Object> handleHostAction(@DestinationVariable Long sessionId,
                                              @Payload Map<String, Object> payload,
                                              Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        String action = (String) payload.get("action");
        Long targetUserId = ((Number) payload.getOrDefault("targetUserId", 0)).longValue();
        
        // 验证用户是否为主持人
        boolean isHost = meetingService.getSessionParticipants(sessionId).stream()
            .filter(p -> p.getUser().getId().equals(userId))
            .anyMatch(com.example.cloudoffice_admin.model.MeetingParticipant::isHost);
        
        if (!isHost) {
            throw new RuntimeException("Only host can perform this action");
        }
        
        // 根据操作类型执行不同的操作
        switch (action) {
            case "MUTE_PARTICIPANT":
                // 实现静音参与者功能
                break;
            case "VIDEO_OFF_PARTICIPANT":
                // 实现关闭参与者视频功能
                break;
            case "REMOVE_PARTICIPANT":
                meetingService.removeParticipant(sessionId, targetUserId);
                break;
            case "MAKE_HOST":
                // 查找目标用户的参与者记录
                com.example.cloudoffice_admin.model.MeetingParticipant targetParticipant = 
                    meetingService.getSessionParticipants(sessionId).stream()
                        .filter(p -> p.getUser().getId().equals(targetUserId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Target participant not found"));
                meetingService.makeHost(targetParticipant.getId());
                break;
        }
        
        return Map.of(
            "event", "HOST_ACTION",
            "action", action,
            "hostUserId", userId,
            "targetUserId", targetUserId
        );
    }

    // 获取用户ID的辅助方法
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        // 假设authentication.getName()返回用户ID
        return Long.valueOf(authentication.getName());
    }
}
