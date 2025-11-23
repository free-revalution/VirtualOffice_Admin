package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class UserPositionRequest {
    private Long spaceId;
    private Long zoneId;
    private int x;
    private int y;
}
