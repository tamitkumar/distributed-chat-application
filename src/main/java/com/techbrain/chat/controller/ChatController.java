package com.techbrain.chat.controller;

import com.techbrain.chat.service.ChatService;
import com.techbrain.chat.service.RoomService;
import com.techbrain.chat.to.Message;
import com.techbrain.chat.to.Room;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Chat Controller
 * 
 * REST API endpoints for chat operations
 */
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Chat API endpoints")
public class ChatController {
    
    private final ChatService chatService;
    private final RoomService roomService;
    
    public ChatController(ChatService chatService, RoomService roomService) {
        this.chatService = chatService;
        this.roomService = roomService;
    }
    
    /**
     * Send a message (REST endpoint)
     * Note: WebSocket is preferred for real-time messaging
     */
    @PostMapping("/messages")
    @Operation(summary = "Send a message", description = "Send a message to a room (REST endpoint)")
    public ResponseEntity<Message> sendMessage(@Valid @RequestBody Message message) {
        Message sent = chatService.sendMessage(message);
        return ResponseEntity.ok(sent);
    }
    
    /**
     * Get message history for a room
     */
    @GetMapping("/rooms/{roomId}/messages")
    @Operation(summary = "Get message history", description = "Get message history for a room")
    public ResponseEntity<List<Message>> getMessageHistory(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "50") int limit) {
        List<Message> messages = chatService.getMessageHistory(roomId, limit);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get paginated messages for a room
     */
    @GetMapping("/rooms/{roomId}/messages/page")
    @Operation(summary = "Get paginated messages", description = "Get paginated messages for a room")
    public ResponseEntity<List<Message>> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Message> messages = chatService.getRoomMessages(roomId, page, size);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get message count for a room
     */
    @GetMapping("/rooms/{roomId}/messages/count")
    @Operation(summary = "Get message count", description = "Get total message count for a room")
    public ResponseEntity<Long> getMessageCount(@PathVariable String roomId) {
        long count = chatService.getMessageCount(roomId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Delete a message
     */
    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete a message", description = "Delete a message by ID")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        chatService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Create a new room
     */
    @PostMapping("/rooms")
    @Operation(summary = "Create a room", description = "Create a new chat room")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room) {
        Room created = roomService.createRoom(room);
        return ResponseEntity.ok(created);
    }
    
    /**
     * Get all rooms
     */
    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms", description = "Get list of all chat rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
    
    /**
     * Get a specific room
     */
    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get a room", description = "Get a specific room by ID")
    public ResponseEntity<Room> getRoom(@PathVariable String roomId) {
        Room room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(room);
    }
    
    /**
     * Delete a room
     */
    @DeleteMapping("/rooms/{roomId}")
    @Operation(summary = "Delete a room", description = "Delete a room by ID")
    public ResponseEntity<Void> deleteRoom(@PathVariable String roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chat service is running");
    }
}
