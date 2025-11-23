package com.example.cloudoffice_admin.dto;

import lombok.Data;

import java.util.Set;

@Data
public class MeetingRoomRequest {

    private String name;
    private String description;
    private int capacity;
    private String roomType; // video, audio, text
    private Long spaceId;
    private Set<String> features; // recording, screen_sharing, whiteboard
    private boolean isActive;
}
