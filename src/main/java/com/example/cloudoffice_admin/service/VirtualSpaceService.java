package com.example.cloudoffice_admin.service;

import com.example.cloudoffice_admin.dto.UserPositionRequest;
import com.example.cloudoffice_admin.dto.VirtualSpaceRequest;
import com.example.cloudoffice_admin.dto.ZoneRequest;
import com.example.cloudoffice_admin.model.User;
import com.example.cloudoffice_admin.model.UserPresence;
import com.example.cloudoffice_admin.model.VirtualSpace;
import com.example.cloudoffice_admin.model.Zone;
import com.example.cloudoffice_admin.repository.UserPresenceRepository;
import com.example.cloudoffice_admin.repository.UserRepository;
import com.example.cloudoffice_admin.repository.VirtualSpaceRepository;
import com.example.cloudoffice_admin.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VirtualSpaceService implements VirtualSpaceServiceInterface {

    @Autowired
    private VirtualSpaceRepository virtualSpaceRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private UserPresenceRepository userPresenceRepository;

    @Autowired
    private UserRepository userRepository;

    // 创建虚拟空间
    @Override
    public VirtualSpace createSpace(VirtualSpaceRequest request, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        VirtualSpace space = new VirtualSpace();
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setWidth(request.getWidth());
        space.setHeight(request.getHeight());
        space.setTheme(request.getTheme());
        space.setPublic(request.isPublic()); // 修改方法名
        space.setCreatorId(creatorId); // 使用creatorId代替creator对象
        space.setSpaceId(UUID.randomUUID().toString());

        return virtualSpaceRepository.save(space);
    }

    // 获取所有公开空间
    @Override
    public List<VirtualSpace> getPublicSpaces() {
        return virtualSpaceRepository.findByIsPublicTrue();
    }

    // 获取用户创建的空间
    @Override
    public List<VirtualSpace> getUserSpaces(Long userId) {
        return virtualSpaceRepository.findByCreatorId(userId);
    }

    // 获取空间详情
    @Override
    public VirtualSpace getSpaceById(Long spaceId) {
        return virtualSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new EntityNotFoundException("Virtual space not found"));
    }

    // 更新空间信息
    @Override
    public VirtualSpace updateSpace(Long spaceId, VirtualSpaceRequest request, Long userId) {
        VirtualSpace space = virtualSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new EntityNotFoundException("Virtual space not found"));

        // 检查是否是创建者
        if (!space.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Only the creator can update the space");
        }

        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setWidth(request.getWidth());
        space.setHeight(request.getHeight());
        space.setTheme(request.getTheme());
        space.setPublic(request.isPublic()); // 修改方法名

        return virtualSpaceRepository.save(space);
    }

    // 删除空间
    @Override
    public void deleteSpace(Long spaceId, Long userId) {
        VirtualSpace space = virtualSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new EntityNotFoundException("Virtual space not found"));

        // 检查是否是创建者
        if (!space.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Only the creator can delete the space");
        }

        virtualSpaceRepository.delete(space);
    }

    // 创建区域
    @Override
    public Zone createZone(ZoneRequest request, Long userId) {
        VirtualSpace space = virtualSpaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new EntityNotFoundException("Virtual space not found"));

        // 检查是否是创建者
        if (!space.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Only the space creator can create zones");
        }

        Zone zone = new Zone();
        zone.setName(request.getName());
        zone.setDescription(request.getDescription());
        zone.setX(request.getX());
        zone.setY(request.getY());
        zone.setWidth(request.getWidth());
        zone.setHeight(request.getHeight());
        zone.setType(request.getType());
        zone.setColor(request.getColor());
        zone.setPrivate(request.isPrivate()); // 修改方法名
        zone.setSpace(space);

        return zoneRepository.save(zone);
    }

    // 获取空间中的所有区域
    @Override
    public List<Zone> getZonesBySpaceId(Long spaceId) {
        return zoneRepository.findBySpaceId(spaceId);
    }

    // 更新用户位置
    @Override
    @Transactional
    public UserPresence updateUserPosition(Long userId, UserPositionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        VirtualSpace space = virtualSpaceRepository.findById(request.getSpaceId())
                .orElseThrow(() -> new EntityNotFoundException("Virtual space not found"));

        Zone zone = null;
        if (request.getZoneId() != null) {
            zone = zoneRepository.findById(request.getZoneId())
                    .orElseThrow(() -> new EntityNotFoundException("Zone not found"));

            // 检查区域是否属于该空间
            if (!zone.getSpace().getId().equals(space.getId())) {
                throw new IllegalArgumentException("Zone does not belong to the specified space");
            }
        }

        // 查找或创建用户存在记录
        UserPresence presence = userPresenceRepository.findByUserId(userId);
        if (presence == null) {
            presence = new UserPresence();
            presence.setUser(user);
            // 设置用户ID字段
            presence.setUserId(userId);
            // 如果需要设置ID标识，可以使用zoneId字段或其他可用字段
            if (zone != null) {
                presence.setZoneId(zone.getId());
            }
        }

        presence.setSpace(space);
        presence.setZone(zone);
        presence.setX(request.getX());
        presence.setY(request.getY());
        presence.setStatus(UserPresence.STATUS_ONLINE); // 使用字符串常量
        presence.setLastActive(LocalDateTime.now()); // 使用LocalDateTime

        return userPresenceRepository.save(presence);
    }

    // 获取空间中的所有在线用户
    @Override
    public List<UserPresence> getUsersInSpace(Long spaceId) {
        return userPresenceRepository.findBySpaceId(spaceId);
    }

    // 获取区域中的所有在线用户
    @Override
    public List<UserPresence> getUsersInZone(Long zoneId) {
        return userPresenceRepository.findByZoneId(zoneId);
    }

    // 用户离开空间
    @Override
    public void userLeaveSpace(Long userId) {
        userPresenceRepository.deleteByUserId(userId);
    }

    // 获取用户当前所在的空间
    @Override
    public UserPresence getUserPresence(Long userId) {
        return userPresenceRepository.findByUserId(userId);
    }
    
    // 新增方法：更新空间状态
    @Override
    public VirtualSpace updateSpaceStatus(Long spaceId, boolean active) {
        VirtualSpace space = virtualSpaceRepository.findById(spaceId)
                .orElseThrow(() -> new EntityNotFoundException("Virtual space not found"));
        
        // 这里可以根据需要实现状态更新逻辑
        return virtualSpaceRepository.save(space);
    }
}
