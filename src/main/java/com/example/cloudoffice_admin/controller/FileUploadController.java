package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.model.FileUpload;
import com.example.cloudoffice_admin.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    // 获取当前用户ID的辅助方法
    private Long getCurrentUserId() {
        // 在实际应用中，这里应该从Spring Security的Authentication对象中获取用户ID
        // 这里为了演示，返回一个固定的用户ID
        // 实际项目中应该替换为：
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.isAuthenticated()) {
        //     return Long.valueOf(authentication.getName()); // 假设用户名是用户ID
        // }
        // throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        return 1L; // 临时值，用于演示
    }

    // 上传文件
    @PostMapping("/upload")
    public ResponseEntity<FileUpload> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = getCurrentUserId();
            FileUpload uploadedFile = fileUploadService.uploadFile(file, userId);
            return new ResponseEntity<>(uploadedFile, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file: " + e.getMessage());
        }
    }

    // 批量上传文件
    @PostMapping("/upload/batch")
    public ResponseEntity<List<FileUpload>> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            Long userId = getCurrentUserId();
            List<FileUpload> uploadedFiles = new java.util.ArrayList<>();

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    FileUpload uploadedFile = fileUploadService.uploadFile(file, userId);
                    uploadedFiles.add(uploadedFile);
                }
            }

            return new ResponseEntity<>(uploadedFiles, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload files: " + e.getMessage());
        }
    }

    // 获取文件信息
    @GetMapping("/{id}")
    public ResponseEntity<FileUpload> getFileInfo(@PathVariable Long id) {
        Optional<FileUpload> fileOpt = fileUploadService.getFileById(id);
        return fileOpt.map(file -> new ResponseEntity<>(file, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }

    // 通过fileId获取文件信息
    @GetMapping("/by-file-id/{fileId}")
    public ResponseEntity<FileUpload> getFileInfoByFileId(@PathVariable String fileId) {
        Optional<FileUpload> fileOpt = fileUploadService.getFileByFileId(fileId);
        return fileOpt.map(file -> new ResponseEntity<>(file, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }

    // 下载文件
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        try {
            Resource resource = fileUploadService.downloadFile(id);
            return prepareDownloadResponse(resource, request);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download file: " + e.getMessage());
        }
    }

    // 通过fileId下载文件
    @GetMapping("/download/by-file-id/{fileId}")
    public ResponseEntity<Resource> downloadFileByFileId(@PathVariable String fileId, HttpServletRequest request) {
        try {
            Resource resource = fileUploadService.downloadFileByFileId(fileId);
            return prepareDownloadResponse(resource, request);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to download file: " + e.getMessage());
        }
    }

    // 获取当前用户的文件列表
    @GetMapping("/my-files")
    public ResponseEntity<List<FileUpload>> getMyFiles() {
        try {
            Long userId = getCurrentUserId();
            List<FileUpload> files = fileUploadService.getUserFiles(userId);
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch files: " + e.getMessage());
        }
    }

    // 按类型获取文件
    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<FileUpload>> getFilesByType(@PathVariable String type) {
        try {
            List<FileUpload> files = fileUploadService.getFilesByType(type);
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch files: " + e.getMessage());
        }
    }

    // 搜索文件
    @GetMapping("/search")
    public ResponseEntity<List<FileUpload>> searchFiles(@RequestParam String keyword) {
        try {
            List<FileUpload> files = fileUploadService.searchFiles(keyword);
            return new ResponseEntity<>(files, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search files: " + e.getMessage());
        }
    }

    // 删除文件
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            fileUploadService.deleteFile(id, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }

    // 获取用户文件统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getUserFileStats() {
        try {
            Long userId = getCurrentUserId();
            long totalSize = fileUploadService.getTotalFileSizeByUser(userId);
            int fileCount = fileUploadService.getFileCountByUser(userId);

            return new ResponseEntity<>(
                    java.util.Map.of(
                            "totalSize", totalSize,
                            "fileCount", fileCount,
                            "formattedSize", formatFileSize(totalSize)
                    ),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get file stats: " + e.getMessage());
        }
    }

    // 准备下载响应的辅助方法
    private ResponseEntity<Resource> prepareDownloadResponse(Resource resource, HttpServletRequest request) throws IOException {
        // 尝试确定文件的内容类型
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        // 如果类型不确定，设置默认值
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // 格式化文件大小的辅助方法
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1048576) return (bytes / 1024) + " KB";
        else if (bytes < 1073741824) return (bytes / 1048576) + " MB";
        else return (bytes / 1073741824) + " GB";
    }
}