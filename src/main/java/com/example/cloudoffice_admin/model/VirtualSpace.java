package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "virtual_spaces")
public class VirtualSpace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    private boolean isPublic = false;
    
    private Long creatorId;
    
    // 添加spaceId字段以支持VirtualSpaceService中的操作
    private String spaceId;
    
    // 添加尺寸和主题字段
    private int width = 1000;
    private int height = 800;
    private String theme = "default";

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 关联关系
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<Zone> zones;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<UserPresence> presences;

    @OneToMany(mappedBy = "virtualSpace", cascade = CascadeType.ALL)
    private List<MeetingRoom> meetingRooms;
}
