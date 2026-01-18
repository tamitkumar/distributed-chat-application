package com.techbrain.chat.service;

import com.techbrain.chat.to.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    /**
     * Register/Login user with phone number (creates if doesn't exist)
     * @param phoneNumber Phone number (e.g., +919876543210)
     * @param username Optional display name (can be null)
     */
    User registerOrLoginWithPhone(String phoneNumber, String username);
    
    /**
     * Get user by phone number
     */
    Optional<User> getUserByPhone(String phoneNumber);
    
    /**
     * Get user by username
     */
    Optional<User> getUserByUsername(String username);
    
    /**
     * Get all online users
     */
    List<User> getOnlineUsers();
    
    /**
     * Mark user as online
     */
    void setUserOnline(String username, boolean online);
    
    /**
     * Update user's last seen time
     */
    void updateLastSeen(String username);
    
    /**
     * Add user to room
     */
    void joinRoom(String username, String roomId);
    
    /**
     * Remove user from room
     */
    void leaveRoom(String username, String roomId);
    
    /**
     * Get all users in a room
     */
    List<User> getUsersInRoom(String roomId);
    
    /**
     * Check if username exists
     */
    boolean usernameExists(String username);
}
