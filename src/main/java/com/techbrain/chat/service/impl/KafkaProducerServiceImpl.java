package com.techbrain.chat.service.impl;

import com.techbrain.chat.service.KafkaProducerService;
import com.techbrain.chat.to.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerServiceImpl.class);

    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaProducerServiceImpl(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, Message message) {
        try {
            CompletableFuture<SendResult<String, Message>> future = kafkaTemplate.send(topic, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("📤 Published to Kafka topic [{}]: {} from {} (Offset: {})",
                        topic, 
                        message.getType(), 
                        message.getSenderUsername(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish to topic [{}]: {}", topic, ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Exception publishing to Kafka topic [{}]", topic, e);
            throw new RuntimeException("Failed to publish message to Kafka", e);
        }
    }

    @Override
    public void publishUnicast(String userId, Message message) {
        // Use single shared topic for all unicast messages
        // Target userId is in message.roomId, consumers will filter
        publish("chat.unicast", message);
    }

    @Override
    public void publishMulticast(String roomId, Message message) {
        // Use single shared topic for all multicast messages
        // Target roomId is in message.roomId, consumers will filter
        publish("chat.multicast", message);
    }

    @Override
    public void publishBroadcast(Message message) {
        publish("chat.broadcast", message);
    }
}

