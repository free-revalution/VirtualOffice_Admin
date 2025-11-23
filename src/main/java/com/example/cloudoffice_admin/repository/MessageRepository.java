package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 按频道ID查找消息，按时间倒序排列
    Page<Message> findByChannelIdOrderByCreatedAtDesc(Long channelId, Pageable pageable);

    // 查找某个频道中特定时间之后的消息
    List<Message> findByChannelIdAndCreatedAtAfter(Long channelId, LocalDateTime timestamp);

    // 查找用户发送的消息
    Page<Message> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);

    // 查找未读消息数量
    @Query("SELECT COUNT(m) FROM Message m WHERE m.channel IN (SELECT c FROM ChatChannel c JOIN c.members u WHERE u.id = :userId) AND m.sender.id != :userId AND m.isRead = false")
    long countUnreadMessagesByUserId(@Param("userId") Long userId);

    // 查找某个频道中用户的未读消息
    @Query("SELECT m FROM Message m WHERE m.channel.id = :channelId AND m.sender.id != :userId AND m.isRead = false")
    List<Message> findUnreadMessagesByChannelAndUser(@Param("channelId") Long channelId, @Param("userId") Long userId);

    // 标记消息为已读
    @Query("UPDATE Message m SET m.isRead = true WHERE m.channel.id = :channelId AND m.sender.id != :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("channelId") Long channelId, @Param("userId") Long userId);

    // 查找回复某条消息的所有消息
    List<Message> findByReplyToId(Long replyToId);

    // 删除频道中的所有消息
    void deleteByChannelId(Long channelId);

    // 根据消息ID查找，包括软删除的消息
    @Query("SELECT m FROM Message m WHERE m.id = :id")
    Message findWithDeleted(@Param("id") Long id);
}
