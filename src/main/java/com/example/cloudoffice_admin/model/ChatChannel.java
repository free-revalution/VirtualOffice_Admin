package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "chat_channels")
public class ChatChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelId;

    @Column(nullable = false)
    private String name;
    
    private String description;

    private String type = "public"; // public, group, private

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 关联关系
    @ManyToOne
    @JoinColumn(name = "space_id")
    private VirtualSpace space;
    
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "channel_members",
            joinColumns = @JoinColumn(name = "channel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members;

    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private List<Message> messages;

    // 最后一条消息（为了优化查询性能）
    @OneToOne
    private Message lastMessage;
    
    // 为了兼容旧代码，添加setSpaceId方法
    public void setSpaceId(Long spaceId) {
        if (spaceId != null) {
            this.space = new VirtualSpace();
            this.space.setId(spaceId);
        }
    }
}
