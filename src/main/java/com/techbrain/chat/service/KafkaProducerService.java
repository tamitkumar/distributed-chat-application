package com.techbrain.chat.service;

import com.techbrain.chat.to.Message;

/**
 * Service for publishing messages to Kafka topics
 */
public interface KafkaProducerService {
    
    /**
     * Publish a message to a specific Kafka topic
     * @param topic The topic name (e.g., "unicast", "multicast.room-123", "broadcast")
     * @param message The message to publish
     */
    void publish(String topic, Message message);
    
    /**
     * Publish a unicast message to user-specific topic
     * @param userId Target user ID
     * @param message The message
     */
    void publishUnicast(String userId, Message message);
    
    /**
     * Publish a multicast message to room-specific topic
     * @param roomId Room ID
     * @param message The message
     */
    void publishMulticast(String roomId, Message message);
    
    /**
     * Publish a broadcast message to global topic
     * @param message The message
     */
    void publishBroadcast(Message message);
}

