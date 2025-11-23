package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.UserPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, Long> {
    List<UserPresence> findBySpaceId(Long spaceId);
    List<UserPresence> findByZoneId(Long zoneId);
    UserPresence findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
