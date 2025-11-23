package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findBySpaceId(Long spaceId);
    List<Zone> findBySpaceIdAndIsPrivateFalse(Long spaceId);
}
