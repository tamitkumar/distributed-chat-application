package com.techbrain.chat.service;

import com.techbrain.chat.to.ServiceInfo;

import java.util.List;

public interface ServiceDiscoveryService {
    void registerService();
    void updateHeartbeat();
    List<ServiceInfo> discoverServices();
    void unregisterService();
}
