package com.example.cloudoffice_admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatChannelRequest {

    private String name;
    private String description;
    private String type; // public, private, direct, space
    private Long spaceId;
    private List<Long> memberIds;

    // 对于直接消息频道，可能只需要两个用户ID
    private Long otherUserId;
}
