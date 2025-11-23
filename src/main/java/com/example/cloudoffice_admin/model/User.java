package com.example.cloudoffice_admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String avatar;
    private String avatarType; // 'emoji' 或 'image'
    
    // 兼容测试代码，同时保留枚举状态
    @Enumerated(EnumType.STRING)
    private Status status = Status.offline;
    
    // 字符串状态字段，用于兼容测试代码
    private String userStatus = "ACTIVE";

    private String role = "USER"; // 改为大写以匹配测试代码
    
    // 最后活动时间，用于用户活动检查
    private LocalDateTime lastActive;

    // 最后登录时间字段
    private LocalDateTime lastLoginTime;

    // 用户ID字段，用于业务逻辑中的唯一标识
    private String userId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 关联关系
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserPresence> presences;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> messages;

    @ManyToMany(mappedBy = "members")
    private List<ChatChannel> channels;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MeetingParticipant> meetingParticipations;

    public enum Status {
        online, away, busy, offline
    }
    
    // 额外的方法来支持测试代码中的状态更新
    public void setStatus(String statusStr) {
        this.userStatus = statusStr;
        // 如果是在线状态，自动更新最后活动时间
        if ("ACTIVE".equals(statusStr)) {
            this.status = Status.online;
            this.lastActive = LocalDateTime.now();
        } else if ("INACTIVE".equals(statusStr)) {
            this.status = Status.offline;
        }
    }
    
    // 新增方法以支持Status枚举参数
    public void setStatus(User.Status statusEnum) {
        this.status = statusEnum;
        // 根据枚举值设置字符串状态
        if (Status.online.equals(statusEnum)) {
            this.userStatus = "ACTIVE";
            this.lastActive = LocalDateTime.now();
        } else {
            this.userStatus = "INACTIVE";
        }
    }
    
    // 获取字符串状态
    public String getStatus() {
        return this.userStatus;
    }
    
    // 获取枚举状态
    public User.Status getStatusEnum() {
        return this.status;
    }
    
    // userId相关getter和setter方法
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    // lastLoginTime相关getter和setter方法
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
