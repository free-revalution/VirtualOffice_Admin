package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.ChatChannel;
import com.example.cloudoffice_admin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatChannelRepository extends JpaRepository<ChatChannel, Long> {

    // 查找用户加入的所有频道
    @Query("SELECT c FROM ChatChannel c JOIN c.members m WHERE m.id = :userId")
    List<ChatChannel> findByMemberId(@Param("userId") Long userId);

    // 查找空间中的所有频道
    @Query("SELECT c FROM ChatChannel c WHERE c.space.id = :spaceId")
    List<ChatChannel> findBySpaceId(@Param("spaceId") Long spaceId);

    // 查找直接消息频道
    @Query("SELECT c FROM ChatChannel c WHERE c.type = 'DIRECT' AND :user1 MEMBER OF c.members AND :user2 MEMBER OF c.members")
    Optional<ChatChannel> findDirectMessageChannel(@Param("user1") User user1, @Param("user2") User user2);

    // 查找公开频道
    List<ChatChannel> findByType(String type);

    // 根据名称模糊搜索频道
    List<ChatChannel> findByNameContainingIgnoreCase(String name);

    // 创建者关系需要添加creator字段到ChatChannel实体后再实现
    // List<ChatChannel> findByCreatorId(Long creatorId);
}
