package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.model.FileUpload;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface FileUploadService {
    // 文件上传
    FileUpload uploadFile(MultipartFile file, Long userId) throws Exception;
    
    // 获取文件信息
    Optional<FileUpload> getFileById(Long id);
    Optional<FileUpload> getFileByFileId(String fileId);
    
    // 下载文件
    Resource downloadFile(Long id) throws Exception;
    Resource downloadFileByFileId(String fileId) throws Exception;
    
    // 获取用户的文件列表
    List<FileUpload> getUserFiles(Long userId);
    
    // 按类型获取文件
    List<FileUpload> getFilesByType(String type);
    
    // 搜索文件
    List<FileUpload> searchFiles(String keyword);
    
    // 删除文件
    void deleteFile(Long id, Long userId) throws Exception;
    
    // 获取文件统计信息
    long getTotalFileSizeByUser(Long userId);
    int getFileCountByUser(Long userId);
    
    // 检查文件是否存在
    boolean existsByFileId(String fileId);
}