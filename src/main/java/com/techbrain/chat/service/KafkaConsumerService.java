package com.techbrain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbrain.chat.to.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka Consumer Service
 * Listens to Kafka topics and forwards messages to connected WebSocket clients
 */
@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Value("${app.server-id}")
    private String serverId;

    private final ObjectMapper objectMapper;
    private final RoomService roomService;

    // WebSocket session management
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    public KafkaConsumerService(ObjectMapper objectMapper, RoomService roomService) {
        this.objectMapper = objectMapper;
        this.roomService = roomService;
    }

    // ============ SESSION MANAGEMENT ============

    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
    }

    public void registerUserSession(String userId, String sessionId) {
        userSessions.put(userId, sessionId);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
    }

    public void removeUserSession(String userId) {
        userSessions.remove(userId);
    }

    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }

    public Map<String, String> getUserSessions() {
        return userSessions;
    }

    // ============ KAFKA LISTENERS ============

    /**
     * Listen to BROADCAST messages (single global topic)
     */
    @KafkaListener(topics = "chat.broadcast", groupId = "${app.server-id}")
    public void consumeBroadcast(@Payload Message message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        
        // Skip messages sent by this server (already delivered locally)
        if (serverId.equals(message.getServerId())) {
            log.debug("⏭️ Skipping BROADCAST message from own server");
            return;
        }

        log.info("📥 Received BROADCAST from Kafka (Offset: {}): {} from {}",
            offset, message.getContent(), message.getSenderUsername());

        // Send to all connected sessions on this server
        int delivered = 0;
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                sendMessage(session, message);
                delivered++;
            }
        }

        log.info("✅ BROADCAST delivered to {} local users", delivered);
    }

    /**
     * Listen to MULTICAST messages (single shared topic)
     * Filter: Only deliver to room members connected to THIS server
     */
    @KafkaListener(topics = "chat.multicast", groupId = "${app.server-id}")
    public void consumeMulticast(@Payload Message message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        
        // Skip messages sent by this server (already delivered locally)
        if (serverId.equals(message.getServerId())) {
            log.debug("⏭️ Skipping MULTICAST message from own server");
            return;
        }

        String roomId = message.getRoomId();
        log.info("📥 Received MULTICAST from Kafka for room {} (Offset: {}): {} from {}",
            roomId, offset, message.getContent(), message.getSenderUsername());

        // Get room members and send to connected ones on this server
        var members = roomService.getRoomMembers(roomId);
        int delivered = 0;

        for (String userId : members) {
            String sessionId = userSessions.get(userId);
            if (sessionId != null) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, message);
                    delivered++;
                }
            }
        }

        log.info("✅ MULTICAST delivered to {} local room members", delivered);
    }

    /**
     * Listen to UNICAST messages (single shared topic)
     * Filter: Only deliver if target user is connected to THIS server
     */
    @KafkaListener(topics = "chat.unicast", groupId = "${app.server-id}")
    public void consumeUnicast(@Payload Message message,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.OFFSET) long offset) {
        
        // Skip messages sent by this server (already delivered locally)
        if (serverId.equals(message.getServerId())) {
            log.debug("⏭️ Skipping UNICAST message from own server");
            return;
        }

        // Target userId is in message.roomId (for UNICAST, roomId = target userId)
        String targetUserId = message.getRoomId();
        
        // Check if target user is connected to THIS server
        String sessionId = userSessions.get(targetUserId);
        if (sessionId != null) {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                log.info("📥 Received UNICAST from Kafka for user {} (Offset: {}): {} from {}",
                    targetUserId, offset, message.getContent(), message.getSenderUsername());
                sendMessage(session, message);
                log.info("✅ UNICAST delivered to user {}", targetUserId);
            } else {
                log.warn("⚠️ User {} session not open on this server", targetUserId);
            }
        } else {
            // User not on this server - that's OK, they're on another server
            log.debug("ℹ️ User {} not connected to this server (Offset: {})", targetUserId, offset);
        }
    }

    // ============ HELPER METHODS ============

    private void sendMessage(WebSocketSession session, Message message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
        }
    }
}

