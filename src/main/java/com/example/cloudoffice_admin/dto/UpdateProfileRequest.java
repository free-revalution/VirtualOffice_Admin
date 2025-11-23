package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String avatar;
    private String avatarType;
}
