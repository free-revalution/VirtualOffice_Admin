package com.example.cloudoffice_admin.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
// import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "meeting_sessions")
public class MeetingSession {

    @SuppressWarnings("deprecation")
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "meeting_room_id")
    private MeetingRoom meetingRoom;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status")
    private String status; // scheduled, active, ended, cancelled

    @Column(name = "recording_enabled")
    private boolean recordingEnabled = false;

    @Column(name = "screen_sharing_enabled")
    private boolean screenSharingEnabled = true;

    @Column(name = "chat_enabled")
    private boolean chatEnabled = true;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<MeetingParticipant> participants = new HashSet<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private Set<Recording> recordings = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.sessionId == null) {
            this.sessionId = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 添加参与者
    public void addParticipant(MeetingParticipant participant) {
        participants.add(participant);
        participant.setSession(this);
    }

    // 移除参与者
    public void removeParticipant(MeetingParticipant participant) {
        participants.remove(participant);
        participant.setSession(null);
    }
}
