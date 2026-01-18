package com.techbrain.chat.service;

import com.techbrain.chat.to.Room;

import java.util.List;
import java.util.Set;

/**
 * Room Service Interface
 * 
 * Handles chat room operations
 */
public interface RoomService {
    
    /**
     * Create a new room
     */
    Room createRoom(Room room);
    
    /**
     * Create a room with parameters
     */
    Room createRoom(String name, String description, String createdBy, boolean isPrivate);
    
    /**
     * Get all rooms
     */
    List<Room> getAllRooms();
    
    /**
     * Get a room by ID
     */
    Room getRoomById(String roomId);
    
    /**
     * Delete a room
     */
    void deleteRoom(String roomId);
    
    /**
     * Join a room
     */
    boolean joinRoom(String roomId, String userId);
    
    /**
     * Leave a room
     */
    void leaveRoom(String roomId, String userId);
    
    /**
     * Get room members
     */
    Set<String> getRoomMembers(String roomId);
    
    /**
     * Check if room exists
     */
    boolean roomExists(String roomId);
}
