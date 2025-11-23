package com.example.cloudoffice_admin.service.impl;

import com.example.cloudoffice_admin.dto.ChatChannelRequest;
import com.example.cloudoffice_admin.dto.MessageRequest;
import com.example.cloudoffice_admin.model.ChatChannel;
import com.example.cloudoffice_admin.model.Message;
import com.example.cloudoffice_admin.model.Message.MessageType;
import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.repository.ChatChannelRepository;
import com.example.cloudoffice_admin.repository.MessageRepository;
import com.example.cloudoffice_admin.repository.UserRepository;
import com.example.cloudoffice_admin.service.ChatService;
import com.example.cloudoffice_admin.service.TranslationService;
import com.example.cloudoffice_admin.service.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatChannelRepository chatChannelRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private WebSocketSessionManager sessionManager;

    @Override
    @Transactional
    public ChatChannel createChannel(ChatChannelRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ChatChannel channel = new ChatChannel();
        channel.setName(request.getName());
        channel.setDescription(request.getDescription());
        channel.setType(request.getType());
        channel.setSpaceId(request.getSpaceId());
        channel.setCreator(creator);
        channel.setChannelId(UUID.randomUUID().toString());
        channel.setCreatedAt(LocalDateTime.now());

        // 添加创建者为成员
        Set<User> members = new HashSet<>();
        members.add(creator);
        channel.setMembers(members);

        return chatChannelRepository.save(channel);
    }

    @Override
    public ChatChannel updateChannel(Long channelId, ChatChannelRequest request) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        channel.setName(request.getName());
        channel.setDescription(request.getDescription());
        channel.setType(request.getType());
        channel.setUpdatedAt(LocalDateTime.now());

        return chatChannelRepository.save(channel);
    }

    @Override
    @Transactional
    public void deleteChannel(Long channelId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        // 删除频道中的所有消息
        messageRepository.deleteByChannelId(channelId);
        
        // 删除频道
        chatChannelRepository.delete(channel);
    }

    @Override
    public Optional<ChatChannel> getChannelById(Long channelId) {
        return chatChannelRepository.findById(channelId);
    }

    @Override
    public List<ChatChannel> getUserChannels(Long userId) {
        return chatChannelRepository.findByMemberId(userId);
    }

    @Override
    public List<ChatChannel> getSpaceChannels(Long spaceId) {
        return chatChannelRepository.findBySpaceId(spaceId);
    }

    @Override
    public ChatChannel addMemberToChannel(Long channelId, Long userId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        channel.getMembers().add(user);
        return chatChannelRepository.save(channel);
    }

    @Override
    public void removeMemberFromChannel(Long channelId, Long userId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        channel.getMembers().remove(user);
        chatChannelRepository.save(channel);
    }

    @Override
    public boolean isMemberInChannel(Long channelId, Long userId) {
        ChatChannel channel = chatChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        return channel.getMembers().stream()
                .anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public ChatChannel getOrCreateDirectMessageChannel(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User user2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 查找是否已存在直接消息频道
        Optional<ChatChannel> existingChannel = chatChannelRepository.findDirectMessageChannel(user1, user2);
        if (existingChannel.isPresent()) {
            return existingChannel.get();
        }

        // 创建新的直接消息频道
        ChatChannelRequest request = new ChatChannelRequest();
        request.setName("DM with " + user1.getUsername() + " and " + user2.getUsername());
        request.setType("DIRECT");
        request.setDescription("Direct message channel");

        ChatChannel channel = createChannel(request, user1Id);
        // 添加第二个用户
        channel.getMembers().add(user2);
        return chatChannelRepository.save(channel);
    }

    @Override
    public Message sendMessage(MessageRequest request, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        ChatChannel channel = chatChannelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        // 验证发送者是否在频道中
        if (!isMemberInChannel(channel.getId(), senderId)) {
            throw new RuntimeException("User is not a member of this channel");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setChannel(channel);
        message.setContent(request.getContent());
        message.setType(MessageType.valueOf(request.getType().toUpperCase()));
        message.setMessageId(UUID.randomUUID().toString());
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);
        message.setIsDeleted(false);

        // 设置回复消息
        if (request.getReplyToId() != null) {
            Message replyToMessage = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new RuntimeException("Reply message not found"));
            message.setReplyTo(replyToMessage);
        }

        // 设置附件
        if (request.getAttachments() != null) {
            message.setAttachments(request.getAttachments());
        }

        // 保存消息
        Message savedMessage = messageRepository.save(message);

        // 更新频道的最后消息
        channel.setLastMessage(savedMessage);
        chatChannelRepository.save(channel);

        // 通过WebSocket发送消息给频道所有成员
        for (User member : channel.getMembers()) {
            if (!member.getId().equals(senderId)) {
                sessionManager.sendMessageToUser(member.getId(), "NEW_MESSAGE", savedMessage);
            }
        }

        return savedMessage;
    }

    @Override
    public Page<Message> getChannelMessages(Long channelId, Pageable pageable) {
        return messageRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // 检查是否是发送者或频道管理员
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Only the sender can delete the message");
        }

        // 软删除消息
        message.setIsDeleted(true);
        messageRepository.save(message);
    }

    @Override
    public Message editMessage(Long messageId, String newContent, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // 检查是否是发送者
        if (!message.getSender().getId().equals(userId)) {
            throw new RuntimeException("Only the sender can edit the message");
        }

        message.setContent(newContent);
        message.setUpdatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long channelId, Long userId) {
        messageRepository.markMessagesAsRead(channelId, userId);
    }

    @Override
    public long getUnreadMessageCount(Long userId) {
        return messageRepository.countUnreadMessagesByUserId(userId);
    }

    @Override
    public Message translateMessage(Long messageId, String targetLanguage, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // 检查用户是否在频道中
        if (!isMemberInChannel(message.getChannel().getId(), userId)) {
            throw new RuntimeException("User is not a member of this channel");
        }

        // 检测源语言
        String sourceLanguage = translationService.detectLanguage(message.getContent());
        
        // 如果源语言和目标语言相同，直接返回消息
        if (sourceLanguage.equals(targetLanguage)) {
            return message;
        }

        // 翻译消息内容
        String translatedContent = translationService.translateText(
                message.getContent(), sourceLanguage, targetLanguage);

        // 更新消息的翻译信息
        message.setTranslatedContent(translatedContent);
        message.setSourceLanguage(sourceLanguage);
        message.setTargetLanguage(targetLanguage);

        return messageRepository.save(message);
    }

    @Override
    public Message replyToMessage(Long messageId, MessageRequest request, Long senderId) {
        // 设置回复ID并发送消息
        request.setReplyToId(messageId);
        return sendMessage(request, senderId);
    }
}
