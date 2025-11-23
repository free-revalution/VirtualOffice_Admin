package com.example.cloudoffice_admin.dto;

import com.example.cloudoffice_admin.model.User;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private User user;
}
