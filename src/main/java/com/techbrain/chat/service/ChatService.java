package com.techbrain.chat.service;

import com.techbrain.chat.to.Message;

import java.util.List;

/**
 * Chat Service Interface
 * 
 * Handles core chat functionality:
 * - Sending messages
 * - Retrieving message history
 * - Broadcasting messages across servers
 */
public interface ChatService {
    
    /**
     * Send a message
     * - Saves to database
     * - Publishes to Redis (for distribution across servers)
     * 
     * @param message Message to send
     * @return Saved message with ID
     */
    Message sendMessage(Message message);
    
    /**
     * Get message history for a room
     * 
     * @param roomId Room ID
     * @param limit Maximum number of messages to retrieve
     * @return List of messages (most recent first)
     */
    List<Message> getMessageHistory(String roomId, int limit);
    
    /**
     * Get all messages for a room (paginated)
     * 
     * @param roomId Room ID
     * @param page Page number (0-based)
     * @param size Page size
     * @return List of messages
     */
    List<Message> getRoomMessages(String roomId, int page, int size);
    
    /**
     * Delete a message
     * 
     * @param messageId Message ID
     */
    void deleteMessage(String messageId);
    
    /**
     * Get total message count for a room
     * 
     * @param roomId Room ID
     * @return Total message count
     */
    long getMessageCount(String roomId);
}
