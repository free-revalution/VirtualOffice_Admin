package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.dto.UserPositionRequest;
import com.example.cloudoffice_admin.dto.VirtualSpaceRequest;
import com.example.cloudoffice_admin.dto.ZoneRequest;
import com.example.cloudoffice_admin.model.UserPresence;
import com.example.cloudoffice_admin.model.VirtualSpace;
import com.example.cloudoffice_admin.model.Zone;

import java.rmi.server.UID;
import java.util.List;

public interface VirtualSpaceServiceInterface {
    // 空间管理
    VirtualSpace createSpace(VirtualSpaceRequest request, Long creatorId);
    VirtualSpace updateSpace(Long spaceId, VirtualSpaceRequest request, Long userId);
    void deleteSpace(Long spaceId, Long userId);
    VirtualSpace getSpaceById(Long spaceId);
    List<VirtualSpace> getPublicSpaces();
    List<VirtualSpace> getUserSpaces(Long userId);
    
    // 区域管理
    Zone createZone(ZoneRequest request, Long userId);
    List<Zone> getZonesBySpaceId(Long spaceId);
    
    // 用户存在状态管理
    UserPresence updateUserPosition(Long userId, UserPositionRequest request);
    void userLeaveSpace(Long userId);
    List<UserPresence> getUsersInSpace(Long spaceId);
    List<UserPresence> getUsersInZone(Long zoneId);
    UserPresence getUserPresence(Long userId);
}