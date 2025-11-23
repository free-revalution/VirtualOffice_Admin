package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.MeetingRoom;
import com.example.cloudoffice_admin.model.MeetingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingSessionRepository extends JpaRepository<MeetingSession, UUID> {

    // 根据会话ID查找
    Optional<MeetingSession> findBySessionId(String sessionId);

    // 根据会议室查找会话
    List<MeetingSession> findByMeetingRoom(MeetingRoom meetingRoom);

    // 根据会议室ID查找会话
    List<MeetingSession> findByMeetingRoomId(Long meetingRoomId);

    // 查找特定状态的会话
    List<MeetingSession> findByStatus(String status);

    // 查找活跃的会话
    List<MeetingSession> findByStatusAndEndTimeIsNull(String status);

    // 查找在指定时间范围内的会话
    List<MeetingSession> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查找特定用户作为主持人的会话
    List<MeetingSession> findByHostId(Long hostId);

    // 查找即将开始的会话（接下来30分钟内）
    List<MeetingSession> findByStartTimeBetweenAndStatus(LocalDateTime now, LocalDateTime upcoming, String status);

    // 删除过期的会话
    void deleteByEndTimeBeforeAndStatus(LocalDateTime time, String status);
    
    // 根据会议室ID和状态查找会话
    List<MeetingSession> findByMeetingRoomIdAndStatus(Long meetingRoomId, String status);
    
    // 根据会议室ID、状态和时间范围查找会话
    List<MeetingSession> findByMeetingRoomIdAndStatusAndStartTimeBetween(Long meetingRoomId, String status, LocalDateTime start, LocalDateTime end);
}
