package com.example.cloudoffice_admin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "file_uploads")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileId;
    private String url;
    private String filename;
    private long size;
    private String type;

    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;
    
    // 用于直接存储上传者ID的字段，避免在某些场景下需要懒加载User对象
    @Column(name = "uploader_id", insertable = false, updatable = false)
    private Long uploaderId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
