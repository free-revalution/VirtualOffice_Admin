package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class ParticipantRequest {

    private Long userId;
    private String role; // host, co-host, participant, viewer
    private String displayName;
    private boolean isAudioOn;
    private boolean isVideoOn;
}
