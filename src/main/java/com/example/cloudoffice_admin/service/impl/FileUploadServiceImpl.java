package com.example.cloudoffice_admin.service.impl;

import com.example.cloudoffice_admin.model.FileUpload;
import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.repository.FileUploadRepository;
import com.example.cloudoffice_admin.repository.UserRepository;
import com.example.cloudoffice_admin.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    // 最大文件大小限制 (100MB)
    private final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    // 允许的文件类型
    private final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain", "application/zip", "application/x-rar-compressed"
    );

    @Override
    @Transactional
    public FileUpload uploadFile(MultipartFile file, Long userId) throws Exception {
        // 验证文件
        validateFile(file);

        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // 准备文件信息
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileId = UUID.randomUUID().toString();
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        String filename = fileId + (fileExtension != null ? "." + fileExtension : "");

        // 保存文件到磁盘
        Path targetLocation = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // 保存文件信息到数据库
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        FileUpload fileUpload = new FileUpload();
        fileUpload.setFileId(fileId);
        fileUpload.setFilename(originalFilename);
        fileUpload.setSize(file.getSize());
        fileUpload.setType(file.getContentType());
        fileUpload.setUrl("/api/files/download/" + fileId);
        fileUpload.setUploaderId(userId); // 使用uploaderId代替uploader对象

        return fileUploadRepository.save(fileUpload);
    }

    @Override
    public Optional<FileUpload> getFileById(Long id) {
        return fileUploadRepository.findById(id);
    }

    @Override
    public Optional<FileUpload> getFileByFileId(String fileId) {
        return fileUploadRepository.findByFileId(fileId);
    }

    @Override
    public Resource downloadFile(Long id) throws Exception {
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File not found"));
        return downloadFileByFileId(fileUpload.getFileId());
    }

    @Override
    public Resource downloadFileByFileId(String fileId) throws Exception {
        FileUpload fileUpload = fileUploadRepository.findByFileId(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found"));

        String fileExtension = StringUtils.getFilenameExtension(fileUpload.getFilename());
        String filename = fileId + (fileExtension != null ? "." + fileExtension : "");
        Path filePath = Paths.get(uploadDir).resolve(filename);

        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("Could not read file: " + fileId);
        }
    }

    @Override
    public List<FileUpload> getUserFiles(Long userId) {
        return fileUploadRepository.findByUploaderId(userId);
    }

    @Override
    public List<FileUpload> getFilesByType(String type) {
        return fileUploadRepository.findByType(type);
    }

    @Override
    public List<FileUpload> searchFiles(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return fileUploadRepository.findByFilenameContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional
    public void deleteFile(Long id, Long userId) throws Exception {
        FileUpload fileUpload = fileUploadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("File not found"));

        // 验证是否是文件上传者
        if (!fileUpload.getUploaderId().equals(userId)) {
            throw new IllegalArgumentException("You don't have permission to delete this file");
        }

        // 删除磁盘上的文件
        String fileExtension = StringUtils.getFilenameExtension(fileUpload.getFilename());
        String filename = fileUpload.getFileId() + (fileExtension != null ? "." + fileExtension : "");
        Path filePath = Paths.get(uploadDir).resolve(filename);
        Files.deleteIfExists(filePath);

        // 删除数据库记录
        fileUploadRepository.delete(fileUpload);
    }

    @Override
    public long getTotalFileSizeByUser(Long userId) {
        List<FileUpload> userFiles = fileUploadRepository.findByUploaderId(userId);
        return userFiles.stream().mapToLong(FileUpload::getSize).sum();
    }

    @Override
    public int getFileCountByUser(Long userId) {
        return fileUploadRepository.findByUploaderId(userId).size();
    }

    @Override
    public boolean existsByFileId(String fileId) {
        return fileUploadRepository.findByFileId(fileId).isPresent();
    }

    // 文件验证方法
    private void validateFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size (100MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed");
        }

        // 验证文件名安全性
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid file path");
        }
    }
}