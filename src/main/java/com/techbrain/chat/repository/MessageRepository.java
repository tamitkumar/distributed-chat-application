package com.techbrain.chat.repository;

import com.techbrain.chat.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Message Repository
 * 
 * Data access layer for messages
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    
    /**
     * Find messages by room ID (paginated and sorted)
     */
    List<MessageEntity> findByRoomId(String roomId, Pageable pageable);
    
    /**
     * Find all messages by room ID (ordered by timestamp descending)
     */
    List<MessageEntity> findByRoomIdOrderByTimestampDesc(String roomId);
    
    /**
     * Count messages in a room
     */
    long countByRoomId(String roomId);
    
    /**
     * Delete all messages in a room
     */
    void deleteByRoomId(String roomId);
}
