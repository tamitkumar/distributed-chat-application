package com.techbrain.chat.cofig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbrain.chat.to.Message;
import jakarta.validation.constraints.NotNull;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.server-id}")
    private String serverId;

    // ============ PRODUCER CONFIGURATION ============

    @Bean
    public ProducerFactory<String, Message> producerFactory(@Qualifier("objectMapper") ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Changed: Using custom serializer instead of deprecated Spring's JsonSerializer
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry 3 times
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Exactly-once semantics
        // Removed: No longer need ADD_TYPE_INFO_HEADERS

        return new DefaultKafkaProducerFactory<>(
                config,
                new StringSerializer(),
                new KafkaJsonSerializer<>(objectMapper)  // Using our custom serializer
        );
    }

    @Bean
    public KafkaTemplate<String, Message> kafkaTemplate(ProducerFactory<String, Message> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // ============ CONSUMER CONFIGURATION ============

    @Bean
    public ConsumerFactory<String, Message> consumerFactory(@Qualifier("objectMapper") ObjectMapper objectMapper) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, serverId); // Unique consumer group per server
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Changed: Using custom deserializer instead of Spring's JsonDeserializer
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Start from latest messages
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // Removed: No longer need TRUSTED_PACKAGES, VALUE_DEFAULT_TYPE, USE_TYPE_INFO_HEADERS

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new KafkaJsonDeserializer<>(Message.class, objectMapper)  // Using our custom deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Message> kafkaListenerContainerFactory(
            ConsumerFactory<String, Message> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Message> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3); // 3 consumer threads per server
        return factory;
    }
}

