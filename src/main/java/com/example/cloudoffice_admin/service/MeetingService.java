package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.dto.MeetingRoomRequest;
import com.example.cloudoffice_admin.dto.MeetingSessionRequest;
import com.example.cloudoffice_admin.dto.ParticipantRequest;
import com.example.cloudoffice_admin.model.MeetingRoom;
import com.example.cloudoffice_admin.model.MeetingSession;
import com.example.cloudoffice_admin.model.MeetingParticipant;
import com.example.cloudoffice_admin.model.Recording;

import java.util.List;
import java.util.Optional;

public interface MeetingService {

    // 会议室管理
    MeetingRoom createMeetingRoom(MeetingRoomRequest request, Long creatorId);
    MeetingRoom updateMeetingRoom(Long roomId, MeetingRoomRequest request);
    void deleteMeetingRoom(Long roomId);
    Optional<MeetingRoom> getMeetingRoomById(Long roomId);
    List<MeetingRoom> getMeetingRoomsBySpace(Long spaceId);
    List<MeetingRoom> getActiveMeetingRooms();
    boolean isRoomNameAvailable(String name, Long spaceId);

    // 会议会话管理
    MeetingSession createMeetingSession(MeetingSessionRequest request, Long hostId);
    MeetingSession startMeetingSession(Long sessionId);
    MeetingSession endMeetingSession(Long sessionId);
    Optional<MeetingSession> getMeetingSessionById(Long sessionId);
    List<MeetingSession> getSessionsByRoom(Long roomId);
    List<MeetingSession> getActiveSessions();

    // 参与者管理
    MeetingParticipant joinMeetingSession(Long sessionId, ParticipantRequest request);
    MeetingParticipant updateParticipantStatus(Long participantId, boolean isAudioOn, boolean isVideoOn);
    void leaveMeetingSession(Long participantId);
    List<MeetingParticipant> getSessionParticipants(Long sessionId);
    boolean isUserInSession(Long sessionId, Long userId);
    MeetingParticipant makeHost(Long participantId);
    void removeParticipant(Long sessionId, Long userId);

    // 录制管理
    Recording startRecording(Long sessionId, Long userId);
    Recording stopRecording(Long recordingId);
    Optional<Recording> getRecordingById(Long recordingId);
    List<Recording> getRecordingsBySession(Long sessionId);
    List<Recording> getRecordingsByRoom(Long roomId);
    void deleteRecording(Long recordingId);
    void shareRecording(Long recordingId, boolean isPublic);
}
