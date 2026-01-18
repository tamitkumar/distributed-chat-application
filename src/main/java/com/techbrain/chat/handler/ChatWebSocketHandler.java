package com.techbrain.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbrain.chat.service.ChatService;
import com.techbrain.chat.service.KafkaConsumerService;
import com.techbrain.chat.service.RoomService;
import com.techbrain.chat.to.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import static com.techbrain.chat.utils.MessageType.*;

/**
 * WebSocket Handler for Chat Application
 * 
 * KEY CHANGES FROM REDIS PUB/SUB TO KAFKA:
 * 
 * BEFORE (Redis Pub/Sub):
 * - ChatWebSocketHandler managed sessions AND subscribed to Redis channels
 * - Each server had Redis listeners for broadcast, rooms, and users
 * - Messages were received via Redis callbacks
 * 
 * AFTER (Kafka):
 * - ChatWebSocketHandler manages sessions ONLY
 * - KafkaConsumerService handles Kafka listeners (@KafkaListener)
 * - Sessions are registered with KafkaConsumerService
 * - Kafka delivers messages to KafkaConsumerService, which forwards to WebSocket sessions
 * 
 * BENEFITS OF KAFKA:
 * 1. Message Persistence - Messages are stored, not lost
 * 2. Guaranteed Delivery - Kafka ensures messages reach consumers
 * 3. Replay Support - Can re-read messages from any offset
 * 4. Better Scalability - Kafka handles millions of messages/sec
 * 5. Consumer Groups - Multiple servers can process messages in parallel
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatService chatService;
    private final RoomService roomService;
    private final KafkaConsumerService kafkaConsumerService;
    private final ObjectMapper objectMapper;
    private final com.techbrain.chat.service.UserService userService;
    
    @Value("${app.server-id}")
    private String serverId;

    public ChatWebSocketHandler(ChatService chatService, 
                                RoomService roomService, 
                                KafkaConsumerService kafkaConsumerService,
                                com.techbrain.chat.service.UserService userService,
                                ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.roomService = roomService;
        this.kafkaConsumerService = kafkaConsumerService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Register session with Kafka consumer service
        kafkaConsumerService.registerSession(session.getId(), session);
        
        // Extract userId from query params: ws://localhost:8080/ws/chat?userId=+919876543210
        String query = session.getUri().getQuery();
        String userId = null;
        
        if (query != null && query.contains("userId=")) {
            userId = query.split("userId=")[1].split("&")[0];
        }
        
        if (userId != null) {
            // Register user session mapping
            kafkaConsumerService.registerUserSession(userId, session.getId());
            
            // Mark user as online
            userService.setUserOnline(userId, true);
            userService.updateLastSeen(userId);
            
            log.info("👤 User {} connected to server {} with session {}", 
                userId, serverId, session.getId());
        }
        
        // Send welcome message
        try {
            String welcomeMsg = String.format(
                "{\"type\":\"connected\",\"message\":\"Welcome to server %s (Kafka-powered)!\",\"serverId\":\"%s\",\"userId\":\"%s\"}", 
                serverId, serverId, userId != null ? userId : "guest");
            session.sendMessage(new TextMessage(welcomeMsg));
        } catch (IOException e) {
            log.error("Failed to send welcome message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Get userId from session mapping
        String userId = null;
        for (var entry : kafkaConsumerService.getUserSessions().entrySet()) {
            if (entry.getValue().equals(session.getId())) {
                userId = entry.getKey();
                break;
            }
        }
        
        // Remove session from Kafka consumer service
        kafkaConsumerService.removeSession(session.getId());
        
        if (userId != null) {
            kafkaConsumerService.removeUserSession(userId);
            
            // Mark user as offline
            userService.setUserOnline(userId, false);
            userService.updateLastSeen(userId);
            
            log.info("User {} disconnected from server {}", userId, serverId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
       try {
           log.debug("Received WebSocket message: {}", message.getPayload());
           Message chatMessage = objectMapper.readValue(message.getPayload(), Message.class);
           
           log.info("Processing {} message from {}", chatMessage.getType(), chatMessage.getSenderUsername());

           // Process message based on type
           switch (chatMessage.getType()) {
               case UNICAST:
                   handleUnicastMessage(session, chatMessage);
                   break;
               case MULTICAST:
                   handleMulticastMessage(session, chatMessage);
                   break;
               case BROADCAST:
                   handleBroadcastMessage(session, chatMessage);
                   break;
               default:
                   log.warn("Unknown message type: {}", chatMessage.getType());
           }
       } catch (Exception e) {
           log.error("Failed to process message: {}", e.getMessage(), e);
           try {
               String errorMsg = String.format("{\"type\":\"error\",\"message\":\"Failed to process: %s\"}", e.getMessage());
               session.sendMessage(new TextMessage(errorMsg));
           } catch (IOException ex) {
               log.error("Failed to send error message", ex);
           }
       }
    }

    private void handleUnicastMessage(WebSocketSession session, Message message) {
        // Set server ID and timestamp
        message.setServerId(serverId);
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        
        // Find target user's session ON THIS SERVER
        String targetUserId = message.getRoomId();  // roomId = target userId for unicast
        String targetSessionId = kafkaConsumerService.getUserSessions().get(targetUserId);

        if (targetSessionId != null) {
            WebSocketSession targetSession = kafkaConsumerService.getSessions().get(targetSessionId);
            if (targetSession != null && targetSession.isOpen()) {
                sendMessage(targetSession, message);
                log.info("UNICAST delivered locally to user: {}", targetUserId);
            }
        } else {
            log.info("User {} not on this server, will be delivered via Kafka", targetUserId);
        }

        // Save and route message (Kafka will distribute to other servers)
        chatService.sendMessage(message);
    }

    private void handleMulticastMessage(WebSocketSession session, Message message) {
        // Set server ID and timestamp
        message.setServerId(serverId);
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        
        // Get room members
        var members = roomService.getRoomMembers(message.getRoomId());
        log.info("MULTICAST to room {} with {} members", message.getRoomId(), members.size());

        // Send to all members ON THIS SERVER
        int localDeliveries = 0;
        for (String userId : members) {
            String sessionId = kafkaConsumerService.getUserSessions().get(userId);
            if (sessionId != null) {
                WebSocketSession memberSession = kafkaConsumerService.getSessions().get(sessionId);
                if (memberSession != null && memberSession.isOpen()) {
                    sendMessage(memberSession, message);
                    localDeliveries++;
                }
            }
        }
        
        log.info("MULTICAST delivered to {} local users, routing to other servers via Kafka",
            localDeliveries);

        // Save message (Kafka will distribute to other servers)
        chatService.sendMessage(message);
    }

    private void handleBroadcastMessage(WebSocketSession session, Message message) {
        // Set server ID and timestamp
        message.setServerId(serverId);
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        
        log.info("📡 BROADCAST from {} to all servers", message.getSenderUsername());
        
        // Send to all connected sessions ON THIS SERVER
        int localDeliveries = 0;
        for (WebSocketSession s : kafkaConsumerService.getSessions().values()) {
            if (s.isOpen()) {
                sendMessage(s, message);
                localDeliveries++;
            }
        }
        
        log.info("BROADCAST delivered to {} local users, routing to other servers via Kafka",
            localDeliveries);

        // Save and publish (Kafka will distribute to all servers)
        chatService.sendMessage(message);
    }


    private void sendMessage(WebSocketSession session, Message message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
        }
    }

}
