package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageId;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private ChatChannel channel;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 翻译相关字段
    private String translatedContent;
    private String sourceLanguage;
    private String targetLanguage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private boolean isRead = false;
    private boolean isDeleted = false;

    // 关联到回复的消息
    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    // 附件信息（JSON格式存储）
    @Column(columnDefinition = "TEXT")
    private String attachments;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM, NOTIFICATION
    }
}
