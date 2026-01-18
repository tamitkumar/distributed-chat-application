package com.techbrain.chat.to;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Room Data Transfer Object
 * 
 * Represents a chat room
 */
public class Room {
    
    private String id;                      // Room ID (UUID)
    private String name;                    // Room name
    private String description;             // Room description
    private String createdBy;               // User who created the room
    private LocalDateTime createdAt;        // When room was created
    private Set<String> memberIds;          // Set of user IDs in the room
    private boolean isPrivate;              // Is room private?
    private int maxMembers = 100;           // Maximum members allowed (default 100)
    
    public Room() {
        this.memberIds = new HashSet<>();
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Set<String> getMemberIds() {
        return memberIds;
    }
    
    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = memberIds;
    }
    
    public boolean isPrivate() {
        return isPrivate;
    }
    
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
    
    public int getMaxMembers() {
        return maxMembers;
    }
    
    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }
    
    // Helper methods
    
    public boolean addMember(String userId) {
        if (memberIds.size() >= maxMembers) {
            return false;  // Room full
        }
        return memberIds.add(userId);
    }
    
    public boolean removeMember(String userId) {
        return memberIds.remove(userId);
    }
    
    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }
    
    public int getMemberCount() {
        return memberIds.size();
    }
    
    public boolean isFull() {
        return memberIds.size() >= maxMembers;
    }
}
