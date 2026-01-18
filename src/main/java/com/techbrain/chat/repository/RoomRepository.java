package com.techbrain.chat.repository;

import com.techbrain.chat.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Room Repository
 * 
 * Data access layer for rooms
 */
@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, String> {
    
    /**
     * Find room by name
     */
    Optional<RoomEntity> findByName(String name);
    
    /**
     * Find rooms by creator
     */
    List<RoomEntity> findByCreatedBy(String createdBy);
    
    /**
     * Find public rooms
     */
    List<RoomEntity> findByIsPrivateFalse();
    
    /**
     * Check if room exists by name
     */
    boolean existsByName(String name);
}
