package com.techbrain.chat.service.impl;

import com.techbrain.chat.entity.MessageEntity;
import com.techbrain.chat.repository.MessageRepository;
import com.techbrain.chat.service.ChatService;
import com.techbrain.chat.service.RedisPubSubService;
import com.techbrain.chat.stretegy.MessageRoutingStrategy;
import com.techbrain.chat.to.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Chat Service Implementation
 * 
 * Handles message persistence and distribution
 */
@Service
@Transactional
public class ChatServiceImpl implements ChatService {
    
    private final MessageRepository messageRepository;
    private final RedisPubSubService redisPubSubService;
    private final Map<String, MessageRoutingStrategy> strategies;
    
    @Value("${app.server-id}")
    private String serverId;
    
    public ChatServiceImpl(MessageRepository messageRepository, 
                          RedisPubSubService redisPubSubService,
                          Map<String, MessageRoutingStrategy> strategies) {
        this.messageRepository = messageRepository;
        this.redisPubSubService = redisPubSubService;
        this.strategies = strategies;
    }
    
    @Override
    public Message sendMessage(Message message) {
        // Set server ID and timestamp
        message.setServerId(serverId);
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        
        // Save to database
        MessageEntity entity = toEntity(message);
        MessageEntity saved = messageRepository.save(entity);
        
        // Convert back to DTO
        Message savedMessage = toDTO(saved);
        
        // Route message using strategy (will publish to Redis on correct channel)
        MessageRoutingStrategy strategy = strategies.get(message.getType().name().toLowerCase());
        if (strategy != null) {
            strategy.route(savedMessage);
        }
        
        return savedMessage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessageHistory(String roomId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, 
            Sort.by(Sort.Direction.DESC, "timestamp"));
        
        return messageRepository.findByRoomId(roomId, pageRequest)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Message> getRoomMessages(String roomId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "timestamp"));
        
        return messageRepository.findByRoomId(roomId, pageRequest)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getMessageCount(String roomId) {
        return messageRepository.countByRoomId(roomId);
    }
    
    // Helper methods for entity-DTO conversion
    
    private MessageEntity toEntity(Message message) {
        MessageEntity entity = new MessageEntity();
        // ALWAYS generate a UUID for message ID
        entity.setId(UUID.randomUUID().toString());
        entity.setRoomId(message.getRoomId());
        entity.setSenderId(message.getSenderId());
        entity.setSenderUsername(message.getSenderUsername());
        entity.setContent(message.getContent());
        entity.setType(message.getType());
        entity.setTimestamp(message.getTimestamp());
        entity.setServerId(message.getServerId());
        return entity;
    }
    
    private Message toDTO(MessageEntity entity) {
        Message message = new Message();
        message.setRoomId(entity.getRoomId());
        message.setSenderId(entity.getSenderId());
        message.setSenderUsername(entity.getSenderUsername());
        message.setContent(entity.getContent());
        message.setType(entity.getType());
        message.setTimestamp(entity.getTimestamp());
        message.setServerId(entity.getServerId());
        return message;
    }
}
