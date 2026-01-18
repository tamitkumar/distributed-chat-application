package com.techbrain.chat.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techbrain.chat.utils.MessageType;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private String roomId;                 // Room where message was sent
    private String senderId;                // User who sent the message
    private String senderUsername;          // Username (for quick access)
    private String content;                 // Message content
    private MessageType type;               // Message type (see enum below)
    private LocalDateTime timestamp;        // When message was sent
    private String serverId;                // Which server processed it (for debugging)


    /**
     * get field
     *
     * @return roomId
     */
    public String getRoomId() {
        return this.roomId;
    }

    /**
     * set field
     *
     * @param roomId
     */
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    /**
     * get field
     *
     * @return senderId
     */
    public String getSenderId() {
        return this.senderId;
    }

    /**
     * set field
     *
     * @param senderId
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    /**
     * get field
     *
     * @return senderUsername
     */
    public String getSenderUsername() {
        return this.senderUsername;
    }

    /**
     * set field
     *
     * @param senderUsername
     */
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    /**
     * get field
     *
     * @return content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * set field
     *
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * get field
     *
     * @return type
     */
    public MessageType getType() {
        return this.type;
    }

    /**
     * set field
     *
     * @param type
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * get field
     *
     * @return timestamp
     */
    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * set field
     *
     * @param timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * get field
     *
     * @return serverId
     */
    public String getServerId() {
        return this.serverId;
    }

    /**
     * set field
     *
     * @param serverId
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
