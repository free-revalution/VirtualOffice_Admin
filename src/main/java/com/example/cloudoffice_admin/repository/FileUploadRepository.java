package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {
    // 按上传者ID查找文件
    List<FileUpload> findByUploaderId(Long uploaderId);
    
    // 按文件ID查找文件
    Optional<FileUpload> findByFileId(String fileId);
    
    // 按文件类型查找文件
    List<FileUpload> findByType(String type);
    
    // 按文件大小范围查找文件
    List<FileUpload> findBySizeBetween(long minSize, long maxSize);
    
    // 按文件名模糊查询
    List<FileUpload> findByFilenameContainingIgnoreCase(String filename);
}
