package com.techbrain.chat.controller;

import com.techbrain.chat.service.OtpService;
import com.techbrain.chat.service.UserService;
import com.techbrain.chat.to.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller
 * 
 * Phone-based authentication flow:
 * 1. POST /auth/send-otp → Send OTP to phone
 * 2. POST /auth/verify-otp → Verify OTP and login/register
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final OtpService otpService;
    private final UserService userService;
    
    public AuthController(OtpService otpService, UserService userService) {
        this.otpService = otpService;
        this.userService = userService;
    }
    
    /**
     * Step 1: Send OTP to phone number
     * POST /api/auth/send-otp
     * Body: {"phoneNumber": "+919876543210"}
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Phone number is required"
            ));
        }
        
        // Validate phone number format (basic validation)
        if (!phoneNumber.matches("^\\+?[1-9]\\d{9,14}$")) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid phone number format. Use international format (e.g., +919876543210)"
            ));
        }
        
        boolean sent = otpService.sendOtp(phoneNumber);
        
        if (sent) {
            return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully",
                "phoneNumber", phoneNumber,
                "expiryMinutes", 5,
                "note", "In demo mode, check server console for OTP"
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to send OTP"
            ));
        }
    }
    
    /**
     * Step 2: Verify OTP and login/register user
     * POST /api/auth/verify-otp
     * Body: {"phoneNumber": "+919876543210", "otp": "123456", "username": "Alice"} 
     * Note: username is optional
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String otp = request.get("otp");
        String username = request.get("username");  // Optional
        
        if (phoneNumber == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Phone number and OTP are required"
            ));
        }
        
        // Verify OTP
        boolean verified = otpService.verifyOtp(phoneNumber, otp);
        
        if (!verified) {
            int remaining = otpService.getRemainingAttempts(phoneNumber);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid OTP",
                "remainingAttempts", remaining
            ));
        }
        
        // OTP verified! Register or login user
        User user = userService.registerOrLoginWithPhone(phoneNumber, username);
        
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "user", Map.of(
                "phoneNumber", user.getPhoneNumber(),
                "username", user.getUsername() != null ? user.getUsername() : "User",
                "online", user.isOnline()
            ),
            "note", "Use this phoneNumber as userId in WebSocket: ws://localhost:8080/ws/chat?userId=" + phoneNumber
        ));
    }
    
    /**
     * Check remaining OTP attempts
     * GET /api/auth/otp-attempts/{phoneNumber}
     */
    @GetMapping("/otp-attempts/{phoneNumber}")
    public ResponseEntity<?> getOtpAttempts(@PathVariable String phoneNumber) {
        int remaining = otpService.getRemainingAttempts(phoneNumber);
        return ResponseEntity.ok(Map.of(
            "phoneNumber", phoneNumber,
            "remainingAttempts", remaining,
            "maxAttempts", 3
        ));
    }
}

