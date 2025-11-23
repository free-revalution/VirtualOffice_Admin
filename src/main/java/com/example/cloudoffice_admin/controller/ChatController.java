package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.dto.ChatChannelRequest;
import com.example.cloudoffice_admin.dto.MessageRequest;
import com.example.cloudoffice_admin.dto.TranslationRequest;
import com.example.cloudoffice_admin.model.ChatChannel;
import com.example.cloudoffice_admin.model.Message;
import com.example.cloudoffice_admin.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 获取当前用户ID的辅助方法
    private Long getCurrentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    // 频道管理API
    @PostMapping("/channels")
    public ResponseEntity<ChatChannel> createChannel(@RequestBody ChatChannelRequest request,
                                                   @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        ChatChannel channel = chatService.createChannel(request, userId);
        return new ResponseEntity<>(channel, HttpStatus.CREATED);
    }

    @PutMapping("/channels/{channelId}")
    public ResponseEntity<ChatChannel> updateChannel(@PathVariable Long channelId,
                                                   @RequestBody ChatChannelRequest request) {
        ChatChannel channel = chatService.updateChannel(channelId, request);
        return ResponseEntity.ok(channel);
    }

    @DeleteMapping("/channels/{channelId}")
    public ResponseEntity<Void> deleteChannel(@PathVariable Long channelId) {
        chatService.deleteChannel(channelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/channels/{channelId}")
    public ResponseEntity<ChatChannel> getChannel(@PathVariable Long channelId) {
        return chatService.getChannelById(channelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/channels")
    public ResponseEntity<List<ChatChannel>> getUserChannels(
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ChatChannel> channels = chatService.getUserChannels(userId);
        return ResponseEntity.ok(channels);
    }

    @GetMapping("/spaces/{spaceId}/channels")
    public ResponseEntity<List<ChatChannel>> getSpaceChannels(@PathVariable Long spaceId) {
        List<ChatChannel> channels = chatService.getSpaceChannels(spaceId);
        return ResponseEntity.ok(channels);
    }

    @PostMapping("/channels/{channelId}/members")
    public ResponseEntity<ChatChannel> addMemberToChannel(@PathVariable Long channelId,
                                                        @RequestBody Long userId) {
        ChatChannel channel = chatService.addMemberToChannel(channelId, userId);
        return ResponseEntity.ok(channel);
    }

    @DeleteMapping("/channels/{channelId}/members/{userId}")
    public ResponseEntity<Void> removeMemberFromChannel(@PathVariable Long channelId,
                                                      @PathVariable Long userId) {
        chatService.removeMemberFromChannel(channelId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/channels/{channelId}/members/{userId}")
    public ResponseEntity<Boolean> isMemberInChannel(@PathVariable Long channelId,
                                                  @PathVariable Long userId) {
        boolean isMember = chatService.isMemberInChannel(channelId, userId);
        return ResponseEntity.ok(isMember);
    }

    @GetMapping("/direct-messages/{otherUserId}")
    public ResponseEntity<ChatChannel> getDirectMessageChannel(
            @PathVariable Long otherUserId,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long currentUserId = getCurrentUserId(authentication);
        ChatChannel channel = chatService.getOrCreateDirectMessageChannel(currentUserId, otherUserId);
        return ResponseEntity.ok(channel);
    }

    // 消息管理API
    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request,
                                             @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Message message = chatService.sendMessage(request, userId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<Page<Message>> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = chatService.getChannelMessages(channelId, pageable);
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        chatService.deleteMessage(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<Message> editMessage(
            @PathVariable Long messageId,
            @RequestBody String newContent,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Message message = chatService.editMessage(messageId, newContent, userId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/channels/{channelId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long channelId,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        chatService.markMessagesAsRead(channelId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        long count = chatService.getUnreadMessageCount(userId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/messages/translate")
    public ResponseEntity<Message> translateMessage(
            @RequestBody TranslationRequest request,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Message message = chatService.translateMessage(request.getMessageId(), request.getTargetLanguage(), userId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/messages/{messageId}/reply")
    public ResponseEntity<Message> replyToMessage(
            @PathVariable Long messageId,
            @RequestBody MessageRequest request,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Message message = chatService.replyToMessage(messageId, request, userId);
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
}
