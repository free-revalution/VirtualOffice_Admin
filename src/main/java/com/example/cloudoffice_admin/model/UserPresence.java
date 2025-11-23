package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_presences")
public class UserPresence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // 用于直接存储用户ID的字段，避免在某些场景下需要懒加载User对象
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private VirtualSpace space;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;
    
    // 用于直接存储区域ID的字段，避免在某些场景下需要懒加载Zone对象
    @Column(name = "zone_id", insertable = false, updatable = false)
    private Long zoneId;

    // 位置坐标
    private int x;
    private int y;

    // 从User.Status枚举改为字符串类型的状态字段，与User实体中的userStatus保持一致
    @Column(name = "status")
    private String status = "online"; // online, away, offline, busy
    
    // 添加额外的状态常量，方便在代码中使用
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_AWAY = "away";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_BUSY = "busy";

    @UpdateTimestamp
    private LocalDateTime lastActive;
    // 额外的状态相关方法，用于兼容VirtualSpaceService中的调用
    public void setActive(boolean active) {
        if (active) {
            this.status = STATUS_ONLINE;
        } else {
            this.status = STATUS_OFFLINE;
        }
    }
    
    public boolean isActive() {
        return STATUS_ONLINE.equals(this.status);
    }
}
