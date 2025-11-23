package com.example.cloudoffice_admin.repository;

import com.example.cloudoffice_admin.model.VirtualSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualSpaceRepository extends JpaRepository<VirtualSpace, Long> {
    List<VirtualSpace> findByIsPublicTrue();
    List<VirtualSpace> findByCreatorId(Long creatorId);
}
