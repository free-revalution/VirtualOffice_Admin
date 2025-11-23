package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class ZoneRequest {
    private String name;
    private String description;
    private int x;
    private int y;
    private int width;
    private int height;
    private String type;
    private String color;
    private boolean isPrivate;
    private Long spaceId;
}
