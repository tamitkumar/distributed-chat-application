package com.techbrain.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    private String id;  // Username is the ID
    
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;  // Primary identifier (e.g., +919876543210)
    
    private String username;  // Optional display name
    
    @Column(unique = true)
    private String email;  // Optional
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @ElementCollection
    @CollectionTable(name = "user_rooms", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "room_id")
    private Set<String> roomIds = new HashSet<>();
    
    private boolean online;

    // Getters and Setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Set<String> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(Set<String> roomIds) {
        this.roomIds = roomIds;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}

