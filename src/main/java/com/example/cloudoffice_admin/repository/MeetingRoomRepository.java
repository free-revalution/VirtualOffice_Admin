package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.MeetingRoom;
import com.example.cloudoffice_admin.model.VirtualSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long> {

    // 根据会议室ID查找
    Optional<MeetingRoom> findByMeetingId(String meetingId);

    // 根据虚拟空间查找会议室
    List<MeetingRoom> findByVirtualSpace(VirtualSpace virtualSpace);

    // 根据虚拟空间ID查找会议室
    List<MeetingRoom> findByVirtualSpaceId(Long spaceId);

    // 查找激活状态的会议室
    List<MeetingRoom> findByIsActiveTrue();

    // 根据名称模糊搜索
    List<MeetingRoom> findByNameContainingIgnoreCase(String name);

    // 查找特定容量以上的会议室
    List<MeetingRoom> findByCapacityGreaterThanEqual(int capacity);

    // 检查会议室名称是否已存在
    boolean existsByNameAndVirtualSpaceId(String name, Long spaceId);
}
