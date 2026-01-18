package com.techbrain.chat.service.impl;

import com.techbrain.chat.entity.UserEntity;
import com.techbrain.chat.repository.UserRepository;
import com.techbrain.chat.service.UserService;
import com.techbrain.chat.to.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String ONLINE_USERS_KEY = "online:users";
    private static final String USER_CACHE_KEY = "user:";
    
    public UserServiceImpl(UserRepository userRepository, 
                          RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public User registerOrLoginWithPhone(String phoneNumber, String username) {
        // Check if user exists
        Optional<UserEntity> existingUser = userRepository.findByPhoneNumber(phoneNumber);
        
        if (existingUser.isPresent()) {
            // User exists - just login
            UserEntity user = existingUser.get();
            
            // Update username if provided
            if (username != null && !username.isBlank()) {
                user.setUsername(username);
            }
            
            // Mark as online
            user.setOnline(true);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            
            // Cache in Redis
            cacheUser(user);
            
            System.out.println("User logged in: " + phoneNumber + " (username: " + user.getUsername() + ")");
            return toDTO(user);
            
        } else {
            // New user - register
            UserEntity newUser = new UserEntity();
            newUser.setId(phoneNumber);  // Use phone as ID
            newUser.setPhoneNumber(phoneNumber);
            newUser.setUsername(username != null && !username.isBlank() ? username : "User" + phoneNumber.substring(phoneNumber.length() - 4));
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setLastSeen(LocalDateTime.now());
            newUser.setOnline(true);
            newUser.setRoomIds(new HashSet<>());
            
            UserEntity saved = userRepository.save(newUser);
            
            // Cache in Redis
            cacheUser(saved);
            
            System.out.println("New user registered: " + phoneNumber + " (username: " + saved.getUsername() + ")");
            return toDTO(saved);
        }
    }
    
    @Override
    public Optional<User> getUserByPhone(String phoneNumber) {
        // Try Redis cache first
        String cacheKey = USER_CACHE_KEY + phoneNumber;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached instanceof UserEntity) {
            return Optional.of(toDTO((UserEntity) cached));
        }
        
        // Fallback to database
        Optional<UserEntity> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            // Cache for next time
            cacheUser(user);
            return Optional.of(toDTO(user));
        }
        
        return Optional.empty();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);
        return userOpt.map(this::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getOnlineUsers() {
        // Try Redis first
        var onlineUserPhones = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        
        if (onlineUserPhones != null && !onlineUserPhones.isEmpty()) {
            return onlineUserPhones.stream()
                .map(Object::toString)
                .map(this::getUserByPhone)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }
        
        // Fallback to database
        return userRepository.findByOnline(true)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public void setUserOnline(String userId, boolean online) {
        // userId can be either phoneNumber or username
        Optional<UserEntity> userOpt = getUserEntityByIdOrPhone(userId);
        
        if (userOpt.isEmpty()) {
            System.out.println("⚠️ User not found: " + userId);
            return;
        }
        
        UserEntity user = userOpt.get();
        user.setOnline(online);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
        
        // Update Redis
        if (online) {
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, user.getPhoneNumber());
        } else {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, user.getPhoneNumber());
        }
        
        // Invalidate cache
        String cacheKey = USER_CACHE_KEY + user.getPhoneNumber();
        redisTemplate.delete(cacheKey);
        
        System.out.println(online ? "User online: " + user.getPhoneNumber() : "User offline: " + user.getPhoneNumber());
    }
    
    @Override
    public void updateLastSeen(String userId) {
        Optional<UserEntity> userOpt = getUserEntityByIdOrPhone(userId);
        
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    @Override
    public void joinRoom(String userId, String roomId) {
        Optional<UserEntity> userOpt = getUserEntityByIdOrPhone(userId);
        
        if (userOpt.isEmpty()) {
            System.out.println("User not found: " + userId);
            return;
        }
        
        UserEntity user = userOpt.get();
        user.getRoomIds().add(roomId);
        userRepository.save(user);
        
        // Invalidate cache
        String cacheKey = USER_CACHE_KEY + user.getPhoneNumber();
        redisTemplate.delete(cacheKey);
        
        System.out.println("User " + user.getPhoneNumber() + " joined room: " + roomId);
    }
    
    @Override
    public void leaveRoom(String userId, String roomId) {
        Optional<UserEntity> userOpt = getUserEntityByIdOrPhone(userId);
        
        if (userOpt.isEmpty()) {
            return;
        }
        
        UserEntity user = userOpt.get();
        user.getRoomIds().remove(roomId);
        userRepository.save(user);
        
        // Invalidate cache
        String cacheKey = USER_CACHE_KEY + user.getPhoneNumber();
        redisTemplate.delete(cacheKey);
        
        System.out.println("User " + user.getPhoneNumber() + " left room: " + roomId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersInRoom(String roomId) {
        return userRepository.findAll()
            .stream()
            .filter(user -> user.getRoomIds().contains(roomId))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    // Helper methods
    
    private Optional<UserEntity> getUserEntityByIdOrPhone(String idOrPhone) {
        // Try by phone number first
        Optional<UserEntity> byPhone = userRepository.findByPhoneNumber(idOrPhone);
        if (byPhone.isPresent()) {
            return byPhone;
        }
        
        // Try by username
        return userRepository.findByUsername(idOrPhone);
    }
    
    private void cacheUser(UserEntity user) {
        String cacheKey = USER_CACHE_KEY + user.getPhoneNumber();
        redisTemplate.opsForValue().set(cacheKey, user, 1, TimeUnit.HOURS);
        if (user.isOnline()) {
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, user.getPhoneNumber());
        }
    }
    
    private User toDTO(UserEntity entity) {
        User user = new User();
        user.setPhoneNumber(entity.getPhoneNumber());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setCreatedAt(entity.getCreatedAt());
        user.setLastSeen(entity.getLastSeen());
        user.setRoomIds(entity.getRoomIds());
        user.setOnline(entity.isOnline());
        return user;
    }
}
