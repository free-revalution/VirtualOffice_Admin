package com.example.cloudoffice_admin.controller;

import com.example.cloudoffice_admin.dto.UserPositionRequest;
import com.example.cloudoffice_admin.dto.VirtualSpaceRequest;
import com.example.cloudoffice_admin.dto.ZoneRequest;
import com.example.cloudoffice_admin.model.UserPresence;
import com.example.cloudoffice_admin.model.VirtualSpace;
import com.example.cloudoffice_admin.model.Zone;
import com.example.cloudoffice_admin.security.CustomUserDetails;
import com.example.cloudoffice_admin.service.VirtualSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
public class VirtualSpaceController {

    @Autowired
    private VirtualSpaceService virtualSpaceService;

    // 创建虚拟空间
    @PostMapping
    public ResponseEntity<VirtualSpace> createSpace(@RequestBody VirtualSpaceRequest request) {
        Long userId = getCurrentUserId();
        VirtualSpace space = virtualSpaceService.createSpace(request, userId);
        return new ResponseEntity<>(space, HttpStatus.CREATED);
    }

    // 获取所有公开空间
    @GetMapping("/public")
    public ResponseEntity<List<VirtualSpace>> getPublicSpaces() {
        List<VirtualSpace> spaces = virtualSpaceService.getPublicSpaces();
        return ResponseEntity.ok(spaces);
    }

    // 获取用户创建的空间
    @GetMapping("/my")
    public ResponseEntity<List<VirtualSpace>> getUserSpaces() {
        Long userId = getCurrentUserId();
        List<VirtualSpace> spaces = virtualSpaceService.getUserSpaces(userId);
        return ResponseEntity.ok(spaces);
    }

    // 获取空间详情
    @GetMapping("/{spaceId}")
    public ResponseEntity<VirtualSpace> getSpace(@PathVariable Long spaceId) {
        VirtualSpace space = virtualSpaceService.getSpaceById(spaceId);
        return ResponseEntity.ok(space);
    }

    // 更新空间信息
    @PutMapping("/{spaceId}")
    public ResponseEntity<VirtualSpace> updateSpace(
            @PathVariable Long spaceId,
            @RequestBody VirtualSpaceRequest request) {
        Long userId = getCurrentUserId();
        VirtualSpace updatedSpace = virtualSpaceService.updateSpace(spaceId, request, userId);
        return ResponseEntity.ok(updatedSpace);
    }

    // 删除空间
    @DeleteMapping("/{spaceId}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long spaceId) {
        Long userId = getCurrentUserId();
        virtualSpaceService.deleteSpace(spaceId, userId);
        return ResponseEntity.noContent().build();
    }

    // 创建区域
    @PostMapping("/zones")
    public ResponseEntity<Zone> createZone(@RequestBody ZoneRequest request) {
        Long userId = getCurrentUserId();
        Zone zone = virtualSpaceService.createZone(request, userId);
        return new ResponseEntity<>(zone, HttpStatus.CREATED);
    }

    // 获取空间中的所有区域
    @GetMapping("/{spaceId}/zones")
    public ResponseEntity<List<Zone>> getZonesBySpaceId(@PathVariable Long spaceId) {
        List<Zone> zones = virtualSpaceService.getZonesBySpaceId(spaceId);
        return ResponseEntity.ok(zones);
    }

    // 更新用户位置
    @PutMapping("/position")
    public ResponseEntity<UserPresence> updateUserPosition(@RequestBody UserPositionRequest request) {
        Long userId = getCurrentUserId();
        UserPresence presence = virtualSpaceService.updateUserPosition(userId, request);
        return ResponseEntity.ok(presence);
    }

    // 获取空间中的所有在线用户
    @GetMapping("/{spaceId}/users")
    public ResponseEntity<List<UserPresence>> getUsersInSpace(@PathVariable Long spaceId) {
        List<UserPresence> users = virtualSpaceService.getUsersInSpace(spaceId);
        return ResponseEntity.ok(users);
    }

    // 获取区域中的所有在线用户
    @GetMapping("/zones/{zoneId}/users")
    public ResponseEntity<List<UserPresence>> getUsersInZone(@PathVariable Long zoneId) {
        List<UserPresence> users = virtualSpaceService.getUsersInZone(zoneId);
        return ResponseEntity.ok(users);
    }

    // 用户离开空间
    @PostMapping("/leave")
    public ResponseEntity<Void> leaveSpace() {
        Long userId = getCurrentUserId();
        virtualSpaceService.userLeaveSpace(userId);
        return ResponseEntity.noContent().build();
    }

    // 获取用户当前所在的空间
    @GetMapping("/my-presence")
    public ResponseEntity<UserPresence> getUserPresence() {
        Long userId = getCurrentUserId();
        UserPresence presence = virtualSpaceService.getUserPresence(userId);
        return presence != null ? ResponseEntity.ok(presence) : ResponseEntity.noContent().build();
    }

    // 获取当前登录用户的ID
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        }
        throw new IllegalArgumentException("User not authenticated");
    }
}
