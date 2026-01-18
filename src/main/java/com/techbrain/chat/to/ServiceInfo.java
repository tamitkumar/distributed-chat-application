package com.techbrain.chat.to;

import java.time.LocalDateTime;

public class ServiceInfo {

    private String serviceId;               // Unique service ID
    private String host;                     // Server host
    private int port;                        // Server port
    private LocalDateTime registeredAt;     // When service registered
    private LocalDateTime lastHeartbeat;    // Last heartbeat time
    private boolean isActive;                // Is service active?

    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        this.isActive = true;
    }

    public boolean isStale() {
        return lastHeartbeat.isBefore(LocalDateTime.now().minusSeconds(60));
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
