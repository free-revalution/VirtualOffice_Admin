package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long> {

    // 根据录制ID查找
    Optional<Recording> findByRecordingId(String recordingId);

    // 根据会议室查找录制
    List<Recording> findByMeetingRoomId(Long meetingRoomId);

    // 根据会话查找录制
    @Query("SELECT r FROM Recording r WHERE r.session.id = :sessionId")
    List<Recording> findBySessionId(Long sessionId);

    // 根据用户查找录制（用户启动的）
    List<Recording> findByRecordedById(Long userId);

    // 查找特定状态的录制
    List<Recording> findByStatus(String status);

    // 查找公开的录制
    List<Recording> findByIsPublicTrue();

    // 根据文件名查找
    Optional<Recording> findByFileName(String fileName);

    // 查找文件大小超过特定值的录制
    List<Recording> findByFileSizeGreaterThanEqual(long fileSize);

    // 删除特定会议室的所有录制
    void deleteByMeetingRoomId(Long meetingRoomId);

    // 删除特定会话的所有录制
    @Modifying
    @Query("DELETE FROM Recording r WHERE r.session.id = :sessionId")
    void deleteBySessionId(Long sessionId);
}
