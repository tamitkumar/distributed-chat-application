package com.techbrain.chat.service.impl;

import com.techbrain.chat.entity.MessageEntity;
import com.techbrain.chat.repository.MessageRepository;
import com.techbrain.chat.service.MessageService;
import com.techbrain.chat.service.RedisPubSubService;
import com.techbrain.chat.stretegy.MessageRoutingStrategy;
import com.techbrain.chat.to.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Message Service Implementation
 * 
 * Handles message persistence and routing
 */
@Service
public class MessageServiceImpl implements MessageService {
    
    private final MessageRepository messageRepository;
    private final RedisPubSubService pubSubService;
    private final Map<String, MessageRoutingStrategy> strategies;
    
    @Value("${app.chat.message-history-limit:100}")
    private int messageHistoryLimit;
    
    public MessageServiceImpl(MessageRepository messageRepository, 
                            RedisPubSubService pubSubService, 
                            Map<String, MessageRoutingStrategy> strategies) {
        this.messageRepository = messageRepository;
        this.pubSubService = pubSubService;
        this.strategies = strategies;
    }
    
    @Override
    public void saveMessage(Message message) {
        MessageEntity messageEntity = new MessageEntity();
        BeanUtils.copyProperties(message, messageEntity);
        if (messageEntity.getId() == null) {
            messageEntity.setId(UUID.randomUUID().toString());
        }
        messageRepository.save(messageEntity);
    }
    
    @Override
    public Message sendMessage(Message message) {
        message.setTimestamp(LocalDateTime.now());
        
        MessageEntity messageEntity = new MessageEntity();
        BeanUtils.copyProperties(message, messageEntity);
        if (messageEntity.getId() == null) {
            messageEntity.setId(UUID.randomUUID().toString());
        }
        
        MessageEntity savedMessage = messageRepository.save(messageEntity);
        BeanUtils.copyProperties(savedMessage, message);
        
        // Publish to Redis
        pubSubService.publishToRoom(message.getRoomId(), message);
        
        // Route message using strategy
        MessageRoutingStrategy strategy = strategies.get(message.getType().name().toLowerCase());
        if (strategy != null) {
            strategy.route(message);
        } else {
            System.out.println("No routing strategy found for type: " + message.getType());
        }
        
        System.out.println("Message sent successfully: " + savedMessage.getId());
        return message;
    }
    
    @Override
    public List<Message> getRoomMessages(String roomId) {
        List<MessageEntity> messagesEntity = messageRepository.findByRoomIdOrderByTimestampDesc(roomId)
                .stream()
                .limit(messageHistoryLimit)
                .toList();
        
        System.out.println("Retrieved " + messagesEntity.size() + " messages for room: " + roomId);
        
        List<Message> messages = new ArrayList<>();
        for (MessageEntity entity : messagesEntity) {
            Message message = new Message();
            BeanUtils.copyProperties(entity, message);
            messages.add(message);
        }
        
        return messages;
    }
    
    @Override
    public void deleteMessage(String messageId) {
        System.out.println("Deleting message: " + messageId);
        messageRepository.deleteById(messageId);
        System.out.println("Message deleted: " + messageId);
    }
}
