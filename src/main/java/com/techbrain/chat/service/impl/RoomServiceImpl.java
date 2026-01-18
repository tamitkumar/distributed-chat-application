package com.techbrain.chat.service.impl;

import com.techbrain.chat.entity.RoomEntity;
import com.techbrain.chat.repository.RoomRepository;
import com.techbrain.chat.service.RoomService;
import com.techbrain.chat.to.Room;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Room Service Implementation
 * 
 * Manages chat rooms with Redis caching
 */
@Service
@Transactional
public class RoomServiceImpl implements RoomService {
    
    private final RoomRepository roomRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String ACTIVE_ROOMS_KEY = "active:rooms";
    private static final String ROOM_MEMBERS_KEY = "room:members:";
    private static final String ROOM_CACHE_KEY = "room:";
    
    public RoomServiceImpl(RoomRepository roomRepository, 
                          RedisTemplate<String, Object> redisTemplate) {
        this.roomRepository = roomRepository;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Room createRoom(Room room) {
        // Set ID and timestamp
        if (room.getId() == null) {
            room.setId(UUID.randomUUID().toString());
        }
        if (room.getCreatedAt() == null) {
            room.setCreatedAt(LocalDateTime.now());
        }
        
        // Save to database
        RoomEntity entity = toEntity(room);
        RoomEntity saved = roomRepository.save(entity);
        
        // Cache in Redis
        String cacheKey = ROOM_CACHE_KEY + saved.getId();
        redisTemplate.opsForValue().set(cacheKey, saved, 1, TimeUnit.HOURS);
        
        // Add to active rooms set
        redisTemplate.opsForSet().add(ACTIVE_ROOMS_KEY, saved.getId());
        
        return toDTO(saved);
    }
    
    @Override
    public Room createRoom(String name, String description, String createdBy, boolean isPrivate) {
        Room room = new Room();
        room.setName(name);
        room.setDescription(description);
        room.setCreatedBy(createdBy);
        room.setPrivate(isPrivate);
        return createRoom(room);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Room getRoomById(String roomId) {
        // Try cache first
        String cacheKey = ROOM_CACHE_KEY + roomId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached instanceof RoomEntity) {
            return toDTO((RoomEntity) cached);
        }
        
        // Fallback to database
        RoomEntity entity = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        // Cache for next time
        redisTemplate.opsForValue().set(cacheKey, entity, 1, TimeUnit.HOURS);
        
        return toDTO(entity);
    }
    
    @Override
    public void deleteRoom(String roomId) {
        // Delete from database
        roomRepository.deleteById(roomId);
        
        // Remove from Redis cache
        String cacheKey = ROOM_CACHE_KEY + roomId;
        redisTemplate.delete(cacheKey);
        
        // Remove from active rooms
        redisTemplate.opsForSet().remove(ACTIVE_ROOMS_KEY, roomId);
        
        // Remove members set
        String membersKey = ROOM_MEMBERS_KEY + roomId;
        redisTemplate.delete(membersKey);
    }
    
    @Override
    public boolean joinRoom(String roomId, String userId) {
        // Get room
        RoomEntity room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        // Check if already a member
        if (room.getMemberIds().contains(userId)) {
            return true;
        }
        
        // Check if room is full
        if (room.getMaxMembers() > 0 && room.getMemberIds().size() >= room.getMaxMembers()) {
            return false;
        }
        
        // Add member
        room.getMemberIds().add(userId);
        roomRepository.save(room);
        
        // Update Redis cache
        String membersKey = ROOM_MEMBERS_KEY + roomId;
        redisTemplate.opsForSet().add(membersKey, userId);
        redisTemplate.expire(membersKey, 1, TimeUnit.HOURS);
        
        // Invalidate room cache
        String cacheKey = ROOM_CACHE_KEY + roomId;
        redisTemplate.delete(cacheKey);
        
        return true;
    }
    
    @Override
    public void leaveRoom(String roomId, String userId) {
        // Get room
        RoomEntity room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        // Remove member
        room.getMemberIds().remove(userId);
        roomRepository.save(room);
        
        // Update Redis
        String membersKey = ROOM_MEMBERS_KEY + roomId;
        redisTemplate.opsForSet().remove(membersKey, userId);
        
        // Invalidate room cache
        String cacheKey = ROOM_CACHE_KEY + roomId;
        redisTemplate.delete(cacheKey);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Set<String> getRoomMembers(String roomId) {
        // Try Redis first
        String membersKey = ROOM_MEMBERS_KEY + roomId;
        Set<Object> redisMembers = redisTemplate.opsForSet().members(membersKey);
        
        if (redisMembers != null && !redisMembers.isEmpty()) {
            return redisMembers.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        }
        
        // Fallback to database
        RoomEntity room = roomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found: " + roomId));
        
        // Cache for next time
        Set<String> members = room.getMemberIds();
        if (!members.isEmpty()) {
            redisTemplate.opsForSet().add(membersKey, members.toArray());
            redisTemplate.expire(membersKey, 1, TimeUnit.HOURS);
        }
        
        return members;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean roomExists(String roomId) {
        // Check Redis first
        Boolean exists = redisTemplate.opsForSet().isMember(ACTIVE_ROOMS_KEY, roomId);
        if (Boolean.TRUE.equals(exists)) {
            return true;
        }
        
        // Check database
        return roomRepository.existsById(roomId);
    }
    
    // Helper methods for entity-DTO conversion
    
    private RoomEntity toEntity(Room room) {
        RoomEntity entity = new RoomEntity();
        entity.setId(room.getId());
        entity.setName(room.getName());
        entity.setDescription(room.getDescription());
        entity.setCreatedBy(room.getCreatedBy());
        entity.setCreatedAt(room.getCreatedAt());
        entity.setPrivate(room.isPrivate());
        entity.setMaxMembers(room.getMaxMembers());
        entity.setMemberIds(room.getMemberIds());
        return entity;
    }
    
    private Room toDTO(RoomEntity entity) {
        Room room = new Room();
        room.setId(entity.getId());
        room.setName(entity.getName());
        room.setDescription(entity.getDescription());
        room.setCreatedBy(entity.getCreatedBy());
        room.setCreatedAt(entity.getCreatedAt());
        room.setPrivate(entity.isPrivate());
        room.setMaxMembers(entity.getMaxMembers());
        room.setMemberIds(entity.getMemberIds());
        return room;
    }
}
