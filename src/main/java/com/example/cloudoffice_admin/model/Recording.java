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
@Table(name = "recordings")
public class Recording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recording_id", unique = true, nullable = false)
    private String recordingId;

    @ManyToOne
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingRoom meetingRoom;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private MeetingSession session;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recordedBy; // 启动录制的用户

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private long fileSize; // 字节

    @Column(name = "duration")
    private long duration; // 秒

    @Column(name = "format")
    private String format; // mp4, webm, etc.

    @Column(name = "status")
    private String status; // recording, processing, completed, failed

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_public")
    private boolean isPublic = false;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
