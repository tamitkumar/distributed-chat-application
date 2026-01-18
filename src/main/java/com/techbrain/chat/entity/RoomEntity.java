package com.techbrain.chat.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Room Entity
 * 
 * Represents a chat room in the database
 */
@Entity
@Table(name = "rooms")
@org.hibernate.annotations.DynamicUpdate
public class RoomEntity {
    
    @Id
    @Column(length = 36)
    private String id;  // UUID
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_members", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "user_id", length = 36)
    private Set<String> memberIds = new HashSet<>();
    
    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;
    
    @Column(name = "max_members", nullable = false)
    private int maxMembers = 100;
    
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
}
