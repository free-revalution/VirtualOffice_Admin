package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class VirtualSpaceRequest {
    private String name;
    private String description;
    private int width;
    private int height;
    private String theme;
    private boolean isPublic;
}
