package com.techbrain.chat.cofig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public record KafkaJsonDeserializer<T>(Class<T> targetType, ObjectMapper objectMapper) implements Deserializer<T> {
    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(data, targetType);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON message", e);
        }
    }
}
