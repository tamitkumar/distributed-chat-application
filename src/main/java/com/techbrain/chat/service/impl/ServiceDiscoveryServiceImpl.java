package com.techbrain.chat.service.impl;

import com.techbrain.chat.service.ServiceDiscoveryService;
import com.techbrain.chat.to.ServiceInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ServiceDiscoveryServiceImpl implements ServiceDiscoveryService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.service.discovery.service-id}")
    private String serviceId;

    @Value("${server.port}")
    private int serverPort;

    @Value("${app.service.discovery.enabled:true}")
    private boolean discoveryEnabled;

    private static final String SERVICE_PREFIX = "service:";
    private static final String SERVICES_SET = "services:active";

    public ServiceDiscoveryServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void registerService() {
        if (!discoveryEnabled) {
            System.out.println("Service discovery disabled");
            return;
        }

        try {
            // Get hostname (or use default)
            String host = System.getenv("HOSTNAME");
            if (host == null || host.isEmpty()) {
                host = "localhost";
            }

            // Create service info
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceId(serviceId);
            serviceInfo.setHost(host);
            serviceInfo.setPort(serverPort);

            // Store in Redis
            String key = SERVICE_PREFIX + serviceId;
            redisTemplate.opsForValue().set(key, serviceInfo);
            redisTemplate.expire(key, 90, java.util.concurrent.TimeUnit.SECONDS);

            // Add to active services set
            redisTemplate.opsForSet().add(SERVICES_SET, serviceId);

            System.out.println("Service registered: " + serviceId + " at " + host + ":{}" + serverPort);

        } catch (Exception e) {
            System.out.println("Failed to register service" +  e);
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${app.service.discovery.heartbeat-interval:30}000")
    public void updateHeartbeat() {
        if (!discoveryEnabled) {
            return;
        }

        try {
            String key = SERVICE_PREFIX + serviceId;
            ServiceInfo serviceInfo = (ServiceInfo) redisTemplate.opsForValue().get(key);

            if (serviceInfo != null) {
                serviceInfo.updateHeartbeat();
                redisTemplate.opsForValue().set(key, serviceInfo);
                redisTemplate.expire(key, 90, java.util.concurrent.TimeUnit.SECONDS);
            } else {
                // Service not registered, register again
                registerService();
            }

        } catch (Exception e) {
            System.out.println("Failed to update heartbeat" + e);
        }
    }

    @Override
    public List<ServiceInfo> discoverServices() {
        try {
            // Get all service IDs from set
            Set<Object> serviceIds = redisTemplate.opsForSet().members(SERVICES_SET);

            if (serviceIds == null || serviceIds.isEmpty()) {
                return List.of();
            }

            // Get service info for each ID
            return serviceIds.stream()
                    .map(id -> {
                        String key = SERVICE_PREFIX + id.toString();
                        return (ServiceInfo) redisTemplate.opsForValue().get(key);
                    })
                    .filter(service -> service != null && !service.isStale())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.out.println("Failed to discover services" + e);
            return List.of();
        }
    }

    @Override
    public void unregisterService() {
        if (!discoveryEnabled) {
            return;
        }

        try {
            String key = SERVICE_PREFIX + serviceId;
            redisTemplate.delete(key);
            redisTemplate.opsForSet().remove(SERVICES_SET, serviceId);

            System.out.println("Service unregistered: " + serviceId);

        } catch (Exception e) {
            System.out.println("Failed to unregister service" + e);
        }
    }

}
