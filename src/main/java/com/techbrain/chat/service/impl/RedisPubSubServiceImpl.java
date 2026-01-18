package com.techbrain.chat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techbrain.chat.service.RedisPubSubService;
import com.techbrain.chat.to.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisPubSubServiceImpl implements RedisPubSubService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    private final Map<String, MessageListener> subscriptions = new ConcurrentHashMap<>();

    public RedisPubSubServiceImpl(StringRedisTemplate stringRedisTemplate, 
                                 RedisMessageListenerContainer listenerContainer, 
                                 @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String channel, Message message) {
        try {
            // Convert message to JSON string using primary ObjectMapper (no polymorphic types)
            String messageJson = objectMapper.writeValueAsString(message);
            
            // Publish JSON string to Redis channel
            stringRedisTemplate.convertAndSend(channel, messageJson);

            System.out.println("Published message to channel {}: {}");

        } catch (Exception e) {
            System.out.println("Failed to publish message to channel: {}");
            throw new RuntimeException("Failed to publish message", e);
        }
    }

    @Override
    public void subscribe(String channel, MessageListener listener) {
        try {
            // Create topic (channel)
            ChannelTopic topic = new ChannelTopic(channel);

            // Add listener to container
            // When message arrives on channel, listener.onMessage() is called
            listenerContainer.addMessageListener(listener, topic);

            // Store subscription (for cleanup later)
            subscriptions.put(channel, listener);

            System.out.println("Subscribed to channel: {}");

        } catch (Exception e) {
            System.out.println("Failed to subscribe to channel: {}");
            throw new RuntimeException("Failed to subscribe", e);
        }
    }

    @Override
    public void unsubscribe(String channel) {
        MessageListener listener = subscriptions.remove(channel);
        if (listener != null) {
            ChannelTopic topic = new ChannelTopic(channel);
            listenerContainer.removeMessageListener(listener, topic);
            System.out.println("Unsubscribed from channel: {}");
        }
    }

    @Override
    public void subscribeToRoom(String roomId, MessageListener listener) {
        String channel = "room:" + roomId;
        subscribe(channel, listener);
    }

    @Override
    public void publishToRoom(String roomId, Message message) {
        String channel = "room:" + roomId;
        publish(channel, message);
    }
}
