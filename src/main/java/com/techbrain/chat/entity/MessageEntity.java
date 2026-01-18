package com.techbrain.chat.entity;

import com.techbrain.chat.utils.MessageType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Message Entity
 * 
 * Represents a chat message in the database
 */
@Entity
@Table(name = "messages")
@org.hibernate.annotations.DynamicUpdate
public class MessageEntity {
    
    @Id
    @Column(length = 36)
    private String id;  // UUID
    
    @Column(name = "room_id", nullable = false, length = 36)
    private String roomId;
    
    @Column(name = "sender_id", nullable = false, length = 36)
    private String senderId;
    
    @Column(name = "sender_username", nullable = false, length = 100)
    private String senderUsername;
    
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MessageType type;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "server_id", nullable = false, length = 36)
    private String serverId;
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderUsername() {
        return senderUsername;
    }
    
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
