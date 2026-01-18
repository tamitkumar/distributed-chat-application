package com.techbrain.chat.to;

import java.time.LocalDateTime;
import java.util.Set;

public class User {

    private String phoneNumber;              // Phone number (unique, primary ID)
    private String username;                 // Optional display name
    private String email;                    // Optional email
    private LocalDateTime createdAt;         // When user registered
    private LocalDateTime lastSeen;          // Last active time
    private Set<String> roomIds;            // Rooms user is in
    private boolean isOnline;               // Is user currently online?

    public void joinRoom(String roomId) {
        roomIds.add(roomId);
    }

    public void leaveRoom(String roomId) {
        roomIds.remove(roomId);
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
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
