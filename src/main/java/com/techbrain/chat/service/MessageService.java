package com.techbrain.chat.service;

import com.techbrain.chat.to.Message;

import java.util.List;

/**
 * Message Service Interface
 * 
 * Handles message operations
 */
public interface MessageService {
    
    /**
     * Save a message
     */
    void saveMessage(Message message);
    
    /**
     * Send a message
     */
    Message sendMessage(Message message);
    
    /**
     * Get messages for a room
     */
    List<Message> getRoomMessages(String roomId);
    
    /**
     * Delete a message by ID (String)
     */
    void deleteMessage(String messageId);
}
