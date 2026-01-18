package com.techbrain.chat.repository;

import com.techbrain.chat.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);
    
    Optional<UserEntity> findByUsername(String username);
    
    Optional<UserEntity> findByEmail(String email);
    
    List<UserEntity> findByOnline(boolean online);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}

