package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.dto.MeetingRoomRequest;
import com.example.cloudoffice_admin.dto.MeetingSessionRequest;
import com.example.cloudoffice_admin.dto.ParticipantRequest;
import com.example.cloudoffice_admin.model.MeetingRoom;
import com.example.cloudoffice_admin.model.MeetingSession;
import com.example.cloudoffice_admin.model.MeetingParticipant;
import com.example.cloudoffice_admin.model.Recording;
import com.example.cloudoffice_admin.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    // 获取当前登录用户ID的辅助方法
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return Long.valueOf(authentication.getName()); // 假设authentication.getName()返回用户ID
    }

    // 会议室管理接口
    @PostMapping("/rooms")
    public ResponseEntity<MeetingRoom> createMeetingRoom(@RequestBody MeetingRoomRequest request) {
        MeetingRoom meetingRoom = meetingService.createMeetingRoom(request, getCurrentUserId());
        return new ResponseEntity<>(meetingRoom, HttpStatus.CREATED);
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<MeetingRoom> updateMeetingRoom(
            @PathVariable Long roomId, 
            @RequestBody MeetingRoomRequest request) {
        MeetingRoom updatedRoom = meetingService.updateMeetingRoom(roomId, request);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteMeetingRoom(@PathVariable Long roomId) {
        meetingService.deleteMeetingRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<MeetingRoom> getMeetingRoom(@PathVariable Long roomId) {
        Optional<MeetingRoom> room = meetingService.getMeetingRoomById(roomId);
        return room.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rooms/space/{spaceId}")
    public ResponseEntity<List<MeetingRoom>> getMeetingRoomsBySpace(@PathVariable Long spaceId) {
        List<MeetingRoom> rooms = meetingService.getMeetingRoomsBySpace(spaceId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/active")
    public ResponseEntity<List<MeetingRoom>> getActiveMeetingRooms() {
        List<MeetingRoom> rooms = meetingService.getActiveMeetingRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/check-name")
    public ResponseEntity<Map<String, Boolean>> checkRoomName(
            @RequestParam String name, 
            @RequestParam Long spaceId) {
        boolean isAvailable = meetingService.isRoomNameAvailable(name, spaceId);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    // 会议会话管理接口
    @PostMapping("/sessions")
    public ResponseEntity<MeetingSession> createMeetingSession(@RequestBody MeetingSessionRequest request) {
        MeetingSession session = meetingService.createMeetingSession(request, getCurrentUserId());
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @PostMapping("/sessions/{sessionId}/start")
    public ResponseEntity<MeetingSession> startMeetingSession(@PathVariable Long sessionId) {
        MeetingSession session = meetingService.startMeetingSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<MeetingSession> endMeetingSession(@PathVariable Long sessionId) {
        MeetingSession session = meetingService.endMeetingSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<MeetingSession> getMeetingSession(@PathVariable Long sessionId) {
        Optional<MeetingSession> session = meetingService.getMeetingSessionById(sessionId);
        return session.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions/room/{roomId}")
    public ResponseEntity<List<MeetingSession>> getSessionsByRoom(@PathVariable Long roomId) {
        List<MeetingSession> sessions = meetingService.getSessionsByRoom(roomId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<MeetingSession>> getActiveSessions() {
        List<MeetingSession> sessions = meetingService.getActiveSessions();
        return ResponseEntity.ok(sessions);
    }

    // 参与者管理接口
    @PostMapping("/sessions/{sessionId}/participants")
    public ResponseEntity<MeetingParticipant> joinMeetingSession(
            @PathVariable Long sessionId, 
            @RequestBody ParticipantRequest request) {
        MeetingParticipant participant = meetingService.joinMeetingSession(sessionId, request);
        return new ResponseEntity<>(participant, HttpStatus.CREATED);
    }

    @PutMapping("/participants/{participantId}/status")
    public ResponseEntity<MeetingParticipant> updateParticipantStatus(
            @PathVariable Long participantId, 
            @RequestParam boolean audioOn, 
            @RequestParam boolean videoOn) {
        MeetingParticipant participant = meetingService.updateParticipantStatus(participantId, audioOn, videoOn);
        return ResponseEntity.ok(participant);
    }

    @PostMapping("/participants/{participantId}/leave")
    public ResponseEntity<Void> leaveMeetingSession(@PathVariable Long participantId) {
        meetingService.leaveMeetingSession(participantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{sessionId}/participants")
    public ResponseEntity<List<MeetingParticipant>> getSessionParticipants(@PathVariable Long sessionId) {
        List<MeetingParticipant> participants = meetingService.getSessionParticipants(sessionId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/sessions/{sessionId}/participants/check")
    public ResponseEntity<Map<String, Boolean>> checkUserInSession(
            @PathVariable Long sessionId, 
            @RequestParam Long userId) {
        boolean inSession = meetingService.isUserInSession(sessionId, userId);
        return ResponseEntity.ok(Map.of("inSession", inSession));
    }

    @PostMapping("/participants/{participantId}/make-host")
    public ResponseEntity<MeetingParticipant> makeHost(@PathVariable Long participantId) {
        MeetingParticipant participant = meetingService.makeHost(participantId);
        return ResponseEntity.ok(participant);
    }

    @DeleteMapping("/sessions/{sessionId}/participants/{userId}")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable Long sessionId, 
            @PathVariable Long userId) {
        meetingService.removeParticipant(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    // 录制管理接口
    @PostMapping("/sessions/{sessionId}/recordings/start")
    public ResponseEntity<Recording> startRecording(@PathVariable Long sessionId) {
        Recording recording = meetingService.startRecording(sessionId, getCurrentUserId());
        return new ResponseEntity<>(recording, HttpStatus.CREATED);
    }

    @PostMapping("/recordings/{recordingId}/stop")
    public ResponseEntity<Recording> stopRecording(@PathVariable Long recordingId) {
        Recording recording = meetingService.stopRecording(recordingId);
        return ResponseEntity.ok(recording);
    }

    @GetMapping("/recordings/{recordingId}")
    public ResponseEntity<Recording> getRecording(@PathVariable Long recordingId) {
        Optional<Recording> recording = meetingService.getRecordingById(recordingId);
        return recording.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sessions/{sessionId}/recordings")
    public ResponseEntity<List<Recording>> getRecordingsBySession(@PathVariable Long sessionId) {
        List<Recording> recordings = meetingService.getRecordingsBySession(sessionId);
        return ResponseEntity.ok(recordings);
    }

    @GetMapping("/rooms/{roomId}/recordings")
    public ResponseEntity<List<Recording>> getRecordingsByRoom(@PathVariable Long roomId) {
        List<Recording> recordings = meetingService.getRecordingsByRoom(roomId);
        return ResponseEntity.ok(recordings);
    }

    @DeleteMapping("/recordings/{recordingId}")
    public ResponseEntity<Void> deleteRecording(@PathVariable Long recordingId) {
        meetingService.deleteRecording(recordingId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/recordings/{recordingId}/share")
    public ResponseEntity<Void> shareRecording(
            @PathVariable Long recordingId, 
            @RequestParam boolean isPublic) {
        meetingService.shareRecording(recordingId, isPublic);
        return ResponseEntity.noContent().build();
    }
}
