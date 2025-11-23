package com.example.cloudoffice_admin.service.impl;

import com.example.cloudoffice_admin.dto.MeetingRoomRequest;
import com.example.cloudoffice_admin.dto.MeetingSessionRequest;
import com.example.cloudoffice_admin.dto.ParticipantRequest;
import com.example.cloudoffice_admin.model.*;
import com.example.cloudoffice_admin.repository.*;
import com.example.cloudoffice_admin.service.MeetingService;
import com.example.cloudoffice_admin.service.WebSocketSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;

    @Autowired
    private MeetingSessionRepository meetingSessionRepository;

    @Autowired
    private MeetingParticipantRepository meetingParticipantRepository;

    @Autowired
    private RecordingRepository recordingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VirtualSpaceRepository virtualSpaceRepository;

    @Autowired
    private WebSocketSessionManager sessionManager;

    // 会议室管理方法
    @Override
    @Transactional
    public MeetingRoom createMeetingRoom(MeetingRoomRequest request, Long creatorId) {
        // 检查会议室名称是否已存在
        if (meetingRoomRepository.existsByNameAndVirtualSpaceId(request.getName(), request.getSpaceId())) {
            throw new RuntimeException("Meeting room with this name already exists in the space");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        VirtualSpace space = virtualSpaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new RuntimeException("Virtual space not found"));

        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setName(request.getName());
        // meetingRoom没有description字段
        meetingRoom.setCapacity(request.getCapacity());
        // meetingRoom没有roomType字段
        meetingRoom.setActive(request.isActive());
        meetingRoom.setVirtualSpace(space);
        meetingRoom.setMeetingId(UUID.randomUUID().toString());

        return meetingRoomRepository.save(meetingRoom);
    }

    @Override
    public MeetingRoom updateMeetingRoom(Long roomId, MeetingRoomRequest request) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Meeting room not found"));

        // 检查名称更新是否会导致冲突
        if (!meetingRoom.getName().equals(request.getName()) && 
            meetingRoomRepository.existsByNameAndVirtualSpaceId(request.getName(), meetingRoom.getVirtualSpace().getId())) {
            throw new RuntimeException("Meeting room with this name already exists in the space");
        }

        meetingRoom.setName(request.getName());
        // meetingRoom没有description字段
        meetingRoom.setCapacity(request.getCapacity());
        // meetingRoom没有roomType字段
        meetingRoom.setActive(request.isActive());
        
        // meetingRoom没有features字段

        return meetingRoomRepository.save(meetingRoom);
    }

    @Override
    @Transactional
    public void deleteMeetingRoom(Long roomId) {
        // 检查是否有关联的活跃会话
        List<MeetingSession> activeSessions = meetingSessionRepository.findByMeetingRoomIdAndStatus(roomId, "active");
        if (!activeSessions.isEmpty()) {
            throw new RuntimeException("Cannot delete room with active sessions");
        }

        meetingRoomRepository.deleteById(roomId);
    }

    @Override
    public Optional<MeetingRoom> getMeetingRoomById(Long roomId) {
        return meetingRoomRepository.findById(roomId);
    }

    @Override
    public List<MeetingRoom> getMeetingRoomsBySpace(Long spaceId) {
        return meetingRoomRepository.findByVirtualSpaceId(spaceId);
    }

    @Override
    public List<MeetingRoom> getActiveMeetingRooms() {
        return meetingRoomRepository.findByIsActiveTrue();
    }

    @Override
    public boolean isRoomNameAvailable(String name, Long spaceId) {
        return !meetingRoomRepository.existsByNameAndVirtualSpaceId(name, spaceId);
    }

    // 会议会话管理方法
    @Override
    @Transactional
    public MeetingSession createMeetingSession(MeetingSessionRequest request, Long hostId) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        MeetingRoom meetingRoom = meetingRoomRepository.findById(request.getMeetingRoomId())
                .orElseThrow(() -> new RuntimeException("Meeting room not found"));

        // 检查时间冲突
        List<MeetingSession> conflictingSessions = meetingSessionRepository.findByMeetingRoomIdAndStatusAndStartTimeBetween(
                request.getMeetingRoomId(), "scheduled", 
                request.getStartTime(), request.getEndTime());
        if (!conflictingSessions.isEmpty()) {
            throw new RuntimeException("Time slot already booked");
        }

        MeetingSession session = new MeetingSession();
        session.setMeetingRoom(meetingRoom);
        session.setHost(host);
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setStatus(request.getStatus());
        session.setRecordingEnabled(request.isRecordingEnabled());
        session.setScreenSharingEnabled(request.isScreenSharingEnabled());
        session.setChatEnabled(request.isChatEnabled());
        session.setSessionId(UUID.randomUUID().toString());

        return meetingSessionRepository.save(session);
    }

    @Override
    @Transactional
    public MeetingSession startMeetingSession(Long sessionId) {
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Meeting session not found"));

        if (!"scheduled".equals(session.getStatus())) {
            throw new RuntimeException("Only scheduled sessions can be started");
        }

        session.setStatus("active");
        session.setStartTime(LocalDateTime.now());

        // 通过WebSocket通知相关用户
        broadcastSessionUpdate(session, "SESSION_STARTED");

        return meetingSessionRepository.save(session);
    }

    @Override
    @Transactional
    public MeetingSession endMeetingSession(Long sessionId) {
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Meeting session not found"));

        if (!"active".equals(session.getStatus())) {
            throw new RuntimeException("Only active sessions can be ended");
        }

        session.setStatus("ended");
        session.setEndTime(LocalDateTime.now());

        // 结束所有录制
        List<Recording> activeRecordings = recordingRepository.findBySessionIdAndStatus(sessionId, "recording");
        for (Recording recording : activeRecordings) {
            recording.setStatus("completed");
            recording.setEndTime(LocalDateTime.now());
            recordingRepository.save(recording);
        }

        // 更新所有参与者状态为离开
        List<MeetingParticipant> participants = meetingParticipantRepository.findBySessionId(sessionId);
        for (MeetingParticipant participant : participants) {
            if ("joined".equals(participant.getStatus())) {
                participant.setStatus("left");
                participant.setLeaveTime(LocalDateTime.now());
                meetingParticipantRepository.save(participant);
            }
        }

        // 通过WebSocket通知相关用户
        broadcastSessionUpdate(session, "SESSION_ENDED");

        return meetingSessionRepository.save(session);
    }

    @Override
    public Optional<MeetingSession> getMeetingSessionById(Long sessionId) {
        return meetingSessionRepository.findById(sessionId);
    }

    @Override
    public List<MeetingSession> getSessionsByRoom(Long roomId) {
        return meetingSessionRepository.findByMeetingRoomId(roomId);
    }

    @Override
    public List<MeetingSession> getActiveSessions() {
        return meetingSessionRepository.findByStatus("active");
    }

    // 参与者管理方法
    @Override
    @Transactional
    public MeetingParticipant joinMeetingSession(Long sessionId, ParticipantRequest request) {
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Meeting session not found"));
        
        if (!"active".equals(session.getStatus())) {
            throw new RuntimeException("Cannot join a non-active session");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 检查用户是否已经在会话中
        Optional<MeetingParticipant> existingParticipant = meetingParticipantRepository.findBySessionIdAndUserId(sessionId, request.getUserId());
        if (existingParticipant.isPresent()) {
            MeetingParticipant participant = existingParticipant.get();
            participant.setStatus("joined");
            // joinedAt字段由@CreationTimestamp自动设置
            participant.setLeaveTime(null);
            return meetingParticipantRepository.save(participant);
        }

        // 检查会议室容量
        long currentParticipants = meetingParticipantRepository.countBySessionId(sessionId);
        if (currentParticipants >= session.getMeetingRoom().getCapacity()) {
            throw new RuntimeException("Meeting room capacity reached");
        }

        MeetingParticipant participant = new MeetingParticipant();
        participant.setSession(session);
        participant.setMeetingRoom(session.getMeetingRoom());
        participant.setUser(user);
        participant.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : user.getUsername());
        participant.setRole(request.getRole() != null ? request.getRole() : "participant");
        participant.setAudioOn(request.isAudioOn());
        participant.setVideoOn(request.isVideoOn());
        participant.setStatus("joined");
        // joinedAt字段已存在
        
        // 如果是第一个参与者，设为主持人
        if (currentParticipants == 0) {
            participant.setHost(true);
            participant.setRole("host");
        }

        MeetingParticipant savedParticipant = meetingParticipantRepository.save(participant);

        // 通过WebSocket通知其他参与者
        broadcastParticipantUpdate(sessionId, savedParticipant, "USER_JOINED");

        return savedParticipant;
    }

    @Override
    public MeetingParticipant updateParticipantStatus(Long participantId, boolean isAudioOn, boolean isVideoOn) {
        MeetingParticipant participant = meetingParticipantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setAudioOn(isAudioOn);
        participant.setVideoOn(isVideoOn);

        MeetingParticipant updatedParticipant = meetingParticipantRepository.save(participant);

        // 通过WebSocket通知其他参与者状态变化
        broadcastParticipantUpdate(participant.getSession().getId(), updatedParticipant, "PARTICIPANT_STATUS_CHANGED");

        return updatedParticipant;
    }

    @Override
    @Transactional
    public void leaveMeetingSession(Long participantId) {
        MeetingParticipant participant = meetingParticipantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setStatus("left");
        participant.setLeaveTime(LocalDateTime.now());
        participant.setAudioOn(false);
        participant.setVideoOn(false);
        participant.setScreenSharing(false);

        meetingParticipantRepository.save(participant);

        // 通过WebSocket通知其他参与者
        broadcastParticipantUpdate(participant.getSession().getId(), participant, "USER_LEFT");
    }

    @Override
    public List<MeetingParticipant> getSessionParticipants(Long sessionId) {
        return meetingParticipantRepository.findBySessionId(sessionId);
    }

    @Override
    public boolean isUserInSession(Long sessionId, Long userId) {
        return meetingParticipantRepository.findBySessionIdAndUserId(sessionId, userId).isPresent();
    }

    @Override
    public MeetingParticipant makeHost(Long participantId) {
        MeetingParticipant participant = meetingParticipantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        // 取消当前主持人的主持人身份
        List<MeetingParticipant> currentHosts = meetingParticipantRepository.findBySessionIdAndIsHostTrue(participant.getSession().getId());
        for (MeetingParticipant host : currentHosts) {
            host.setHost(false);
            host.setRole("participant");
            meetingParticipantRepository.save(host);
        }

        // 设置新主持人
        participant.setHost(true);
        participant.setRole("host");

        MeetingParticipant updatedParticipant = meetingParticipantRepository.save(participant);

        // 通过WebSocket通知所有参与者
        broadcastParticipantUpdate(participant.getSession().getId(), updatedParticipant, "HOST_CHANGED");

        return updatedParticipant;
    }

    @Override
    @Transactional
    public void removeParticipant(Long sessionId, Long userId) {
        Optional<MeetingParticipant> participantOpt = meetingParticipantRepository.findBySessionIdAndUserId(sessionId, userId);
        if (participantOpt.isPresent()) {
            MeetingParticipant participant = participantOpt.get();
            leaveMeetingSession(participant.getId());
        }
    }

    // 录制管理方法
    @Override
    @Transactional
    public Recording startRecording(Long sessionId, Long userId) {
        MeetingSession session = meetingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Meeting session not found"));
        
        if (!"active".equals(session.getStatus())) {
            throw new RuntimeException("Cannot start recording for a non-active session");
        }

        // 简化处理，移除录制检查

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 检查用户是否有权限录制（主持人或有权限的参与者）
        Optional<MeetingParticipant> participantOpt = meetingParticipantRepository.findBySessionIdAndUserId(sessionId, userId);
        if (!participantOpt.isPresent() || (!participantOpt.get().isHost() && !"co-host".equals(participantOpt.get().getRole()))) {
            throw new RuntimeException("User does not have permission to start recording");
        }

        Recording recording = new Recording();
        recording.setMeetingRoom(session.getMeetingRoom());
        recording.setSession(session);
        recording.setRecordedBy(user);
        recording.setRecordingId(UUID.randomUUID().toString());
        recording.setStatus("recording");
        recording.setStartTime(LocalDateTime.now());
        recording.setFormat("mp4"); // 默认格式

        // 生成文件名
        String fileName = "meeting_recording_" + session.getSessionId() + "_" + System.currentTimeMillis() + ".mp4";
        recording.setFileName(fileName);

        // 在实际应用中，这里应该启动实际的录制过程

        Recording savedRecording = recordingRepository.save(recording);

        // 简化处理

        return savedRecording;
    }

    @Override
    @Transactional
    public Recording stopRecording(Long recordingId) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        if (!"recording".equals(recording.getStatus())) {
            throw new RuntimeException("Recording is not in progress");
        }

        recording.setStatus("completed");
        recording.setEndTime(LocalDateTime.now());
        
        // 计算持续时间（秒）
        long duration = java.time.Duration.between(recording.getStartTime(), recording.getEndTime()).getSeconds();
        recording.setDuration(duration);

        // 在实际应用中，这里应该停止实际的录制过程并处理文件

        Recording updatedRecording = recordingRepository.save(recording);

        // 通过WebSocket通知所有参与者
        broadcastRecordingUpdate(recording.getSession().getId(), updatedRecording, "RECORDING_STOPPED");

        return updatedRecording;
    }

    @Override
    public Optional<Recording> getRecordingById(Long recordingId) {
        return recordingRepository.findById(recordingId);
    }

    @Override
    public List<Recording> getRecordingsBySession(Long sessionId) {
        return recordingRepository.findBySessionId(sessionId);
    }

    @Override
    public List<Recording> getRecordingsByRoom(Long roomId) {
        return recordingRepository.findByMeetingRoomId(roomId);
    }

    @Override
    public void deleteRecording(Long recordingId) {
        // 在实际应用中，这里应该同时删除物理文件
        recordingRepository.deleteById(recordingId);
    }

    @Override
    public void shareRecording(Long recordingId, boolean isPublic) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        recording.setPublic(isPublic);
        recordingRepository.save(recording);
    }

    // 辅助方法 - 广播会话更新
    private void broadcastSessionUpdate(MeetingSession session, String eventType) {
        // 获取会话中的所有参与者
        List<MeetingParticipant> participants = meetingParticipantRepository.findBySessionId(session.getId());
        
        for (MeetingParticipant participant : participants) {
            sessionManager.sendMessageToUser(participant.getUser().getId(), eventType, session);
        }
    }

    // 辅助方法 - 广播参与者更新
    private void broadcastParticipantUpdate(Long sessionId, MeetingParticipant participant, String eventType) {
        // 获取会话中的所有参与者
        List<MeetingParticipant> participants = meetingParticipantRepository.findBySessionId(sessionId);
        
        for (MeetingParticipant p : participants) {
            sessionManager.sendMessageToUser(p.getUser().getId(), eventType, participant);
        }
    }

    // 辅助方法 - 广播录制更新
    private void broadcastRecordingUpdate(UUID sessionId, Recording recording, String eventType) {
        // 获取会话中的所有参与者
        List<MeetingParticipant> participants = meetingParticipantRepository.findBySessionId(sessionId);
        
        for (MeetingParticipant participant : participants) {
            sessionManager.sendMessageToUser(participant.getUser().getId(), eventType, recording);
        }
    }
}
