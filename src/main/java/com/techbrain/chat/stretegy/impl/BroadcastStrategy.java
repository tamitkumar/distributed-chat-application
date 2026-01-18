package com.techbrain.chat.stretegy.impl;

import com.techbrain.chat.service.KafkaProducerService;
import com.techbrain.chat.stretegy.MessageRoutingStrategy;
import com.techbrain.chat.to.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("broadcast")
public class BroadcastStrategy implements MessageRoutingStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(BroadcastStrategy.class);
    
    private final KafkaProducerService kafkaProducerService;
    
    public BroadcastStrategy(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }
    
    @Override
    public void route(Message message) {
        log.info("Routing BROADCAST message from {}", message.getSenderUsername());
        
        // Publish to Kafka global broadcast topic
        // All servers (Kafka consumers) will receive and deliver to their connected clients
        kafkaProducerService.publishBroadcast(message);
        
        log.info("BROADCAST message routed to Kafka for all servers");
    }
}
