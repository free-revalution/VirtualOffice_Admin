package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    // 根据会议会话查找参与者
    @Query("SELECT p FROM MeetingParticipant p WHERE p.session.id = :sessionId")
    List<MeetingParticipant> findBySessionId(UUID sessionId);

    // 根据会议室查找参与者
    List<MeetingParticipant> findByMeetingRoomId(Long meetingRoomId);

    // 根据用户查找参与者记录
    List<MeetingParticipant> findByUserId(Long userId);

    // 查找特定会话中的特定用户
    @Query("SELECT p FROM MeetingParticipant p WHERE p.session.id = :sessionId AND p.user.id = :userId")
    Optional<MeetingParticipant> findBySessionIdAndUserId(UUID sessionId, Long userId);

    // 查找特定会话中的主持人
    @Query("SELECT p FROM MeetingParticipant p WHERE p.session.id = :sessionId AND p.isHost = true")
    List<MeetingParticipant> findBySessionIdAndIsHostTrue(UUID sessionId);

    // 查找特定会话中状态为joined的参与者
    @Query("SELECT p FROM MeetingParticipant p WHERE p.session.id = :sessionId AND p.status = :status")
    List<MeetingParticipant> findBySessionIdAndStatus(UUID sessionId, String status);

    // 统计特定会话的参与者数量
    @Query("SELECT COUNT(p) FROM MeetingParticipant p WHERE p.session.id = :sessionId")
    long countBySessionId(UUID sessionId);

    // 删除特定会话的所有参与者
    @Modifying
    @Query("DELETE FROM MeetingParticipant p WHERE p.session.id = :sessionId")
    void deleteBySessionId(UUID sessionId);
}
