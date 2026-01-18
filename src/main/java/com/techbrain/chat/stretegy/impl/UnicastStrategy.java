package com.techbrain.chat.stretegy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbrain.chat.service.KafkaProducerService;
import com.techbrain.chat.stretegy.MessageRoutingStrategy;
import com.techbrain.chat.to.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("unicast")
public class UnicastStrategy implements MessageRoutingStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(UnicastStrategy.class);
    
    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;
    
    public UnicastStrategy(StringRedisTemplate stringRedisTemplate, 
                          KafkaProducerService kafkaProducerService,
                          @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void route(Message message) {
        log.info("Routing UNICAST message from {} to {}",
            message.getSenderUsername(), message.getRoomId());  // roomId contains target userId for unicast
        
        try {
            // Store in Redis inbox for offline message delivery (as JSON string)
            String inboxKey = "user:inbox:" + message.getRoomId();
            String messageJson = objectMapper.writeValueAsString(message);
            stringRedisTemplate.opsForList().rightPush(inboxKey, messageJson);
            stringRedisTemplate.expire(inboxKey, 24, TimeUnit.HOURS);
            
            // Publish to Kafka for real-time delivery across all servers
            kafkaProducerService.publishUnicast(message.getRoomId(), message);
            
            log.info("UNICAST message routed to Kafka and Redis inbox");
        } catch (Exception e) {
            log.error("Failed to route UNICAST message", e);
            throw new RuntimeException("Failed to route message", e);
        }
    }
}
