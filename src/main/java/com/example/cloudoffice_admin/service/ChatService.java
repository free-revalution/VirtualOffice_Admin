package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.dto.ChatChannelRequest;
import com.example.cloudoffice_admin.dto.MessageRequest;
import com.example.cloudoffice_admin.model.ChatChannel;
import com.example.cloudoffice_admin.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ChatService {

    // 频道管理
    ChatChannel createChannel(ChatChannelRequest request, Long creatorId);
    ChatChannel updateChannel(Long channelId, ChatChannelRequest request);
    void deleteChannel(Long channelId);
    Optional<ChatChannel> getChannelById(Long channelId);
    List<ChatChannel> getUserChannels(Long userId);
    List<ChatChannel> getSpaceChannels(Long spaceId);
    ChatChannel addMemberToChannel(Long channelId, Long userId);
    void removeMemberFromChannel(Long channelId, Long userId);
    boolean isMemberInChannel(Long channelId, Long userId);
    ChatChannel getOrCreateDirectMessageChannel(Long user1Id, Long user2Id);

    // 消息管理
    Message sendMessage(MessageRequest request, Long senderId);
    Page<Message> getChannelMessages(Long channelId, Pageable pageable);
    void deleteMessage(Long messageId, Long userId);
    Message editMessage(Long messageId, String newContent, Long userId);
    void markMessagesAsRead(Long channelId, Long userId);
    long getUnreadMessageCount(Long userId);
    Message translateMessage(Long messageId, String targetLanguage, Long userId);
    Message replyToMessage(Long messageId, MessageRequest request, Long senderId);
}
