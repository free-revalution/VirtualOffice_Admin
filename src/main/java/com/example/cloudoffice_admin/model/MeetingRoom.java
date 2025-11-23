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
@Table(name = "meeting_rooms")
public class MeetingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String meetingId;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "virtual_space_id")
    private VirtualSpace virtualSpace;

    private int capacity = 10;
    private boolean isLocked = false;
    private boolean isActive = true; // 添加激活状态字段

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 关联关系
    @OneToMany(mappedBy = "meetingRoom", cascade = CascadeType.ALL)
    private List<MeetingParticipant> participants;

    @OneToMany(mappedBy = "meetingRoom", cascade = CascadeType.ALL)
    private List<Recording> recordings;
}
