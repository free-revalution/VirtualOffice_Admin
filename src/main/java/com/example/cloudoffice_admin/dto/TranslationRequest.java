package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class TranslationRequest {

    private Long messageId;
    private String targetLanguage;
}
