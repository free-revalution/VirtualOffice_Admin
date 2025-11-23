package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "meeting_participants")
public class MeetingParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private MeetingSession session;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingRoom meetingRoom;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "role")
    private String role; // host, co-host, participant, viewer

    private boolean isHost = false;
    private boolean isSpeaking = false;
    private boolean hasMic = true;
    private boolean hasCamera = true;
    private boolean hasHandRaised = false;
    
    @Column(name = "is_audio_on")
    private boolean isAudioOn = false;

    @Column(name = "is_video_on")
    private boolean isVideoOn = false;

    @Column(name = "is_screen_sharing")
    private boolean isScreenSharing = false;

    @Column(name = "status")
    private String status; // joined, left, waiting

    @CreationTimestamp
    private LocalDateTime joinedAt;
    
    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
