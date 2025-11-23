package com.example.cloudoffice_admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingSessionRequest {

    private Long meetingRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // scheduled, active, ended, cancelled
    private boolean recordingEnabled;
    private boolean screenSharingEnabled;
    private boolean chatEnabled;
    private String description;
}
