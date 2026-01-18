package com.techbrain.chat.stretegy.impl;

import com.techbrain.chat.service.KafkaProducerService;
import com.techbrain.chat.service.RoomService;
import com.techbrain.chat.stretegy.MessageRoutingStrategy;
import com.techbrain.chat.to.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("multicast")
public class MulticastStrategy implements MessageRoutingStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(MulticastStrategy.class);
    
    private final KafkaProducerService kafkaProducerService;
    private final RoomService roomService;
    
    public MulticastStrategy(KafkaProducerService kafkaProducerService, RoomService roomService) {
        this.kafkaProducerService = kafkaProducerService;
        this.roomService = roomService;
    }
    
    @Override
    public void route(Message message) {
        log.info("Routing MULTICAST message to room: {}", message.getRoomId());
        
        // Get room members count for logging
        var members = roomService.getRoomMembers(message.getRoomId());
        log.info("Room has {} members", members.size());
        
        // Publish to Kafka room-specific topic
        // All servers (Kafka consumers) will receive the message
        kafkaProducerService.publishMulticast(message.getRoomId(), message);
        
        log.info("MULTICAST message routed to Kafka for {} members", members.size());
    }
}
