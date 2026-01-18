package com.techbrain.chat.service;

import com.techbrain.chat.to.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Redis Pub/Sub Service Interface
 * 
 * Handles Redis publish/subscribe operations for distributed messaging
 */
public interface RedisPubSubService {
    
    /**
     * Publish message to a channel
     */
    void publish(String channel, Message message);
    
    /**
     * Publish message (alias for publish)
     */
    default void publishMessage(Message message) {
        publish("chat-messages", message);
    }
    
    /**
     * Subscribe to a channel
     */
    void subscribe(String channel, MessageListener listener);
    
    /**
     * Unsubscribe from a channel
     */
    void unsubscribe(String channel);
    
    /**
     * Subscribe to a specific room
     */
    void subscribeToRoom(String roomId, MessageListener listener);
    
    /**
     * Publish message to a specific room
     */
    void publishToRoom(String roomId, Message message);
}
