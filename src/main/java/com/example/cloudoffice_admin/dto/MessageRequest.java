package com.example.cloudoffice_admin.dto;

import lombok.Data;

@Data
public class MessageRequest {

    private Long channelId;
    private String content;
    private String type; // text, image, file, system, notification
    private Long replyToId;
    private String attachments;
    private boolean needsTranslation = false;
    private String targetLanguage;
}
