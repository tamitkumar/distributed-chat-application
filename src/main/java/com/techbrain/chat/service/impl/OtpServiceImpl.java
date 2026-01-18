package com.techbrain.chat.service.impl;

import com.techbrain.chat.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class OtpServiceImpl implements OtpService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom random = new SecureRandom();
    
    @Value("${app.otp.length:6}")
    private int otpLength;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;
    
    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;
    
    @Value("${app.otp.demo-mode:true}")
    private boolean demoMode;  // For demo, just log OTP instead of sending SMS
    
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String ATTEMPTS_KEY_PREFIX = "otp:attempts:";
    
    public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean sendOtp(String phoneNumber) {
        try {
            // Generate random OTP
            String otp = generateOtp();
            
            // Store in Redis with expiry
            String otpKey = OTP_KEY_PREFIX + phoneNumber;
            redisTemplate.opsForValue().set(otpKey, otp, otpExpiryMinutes, TimeUnit.MINUTES);
            
            // Reset attempts counter
            String attemptsKey = ATTEMPTS_KEY_PREFIX + phoneNumber;
            redisTemplate.opsForValue().set(attemptsKey, "0", otpExpiryMinutes, TimeUnit.MINUTES);
            
            if (demoMode) {
                // DEMO MODE: Just log OTP to console
                System.out.println("╔══════════════════════════════════════╗");
                System.out.println("║           OTP SENT (DEMO MODE)       ║");
                System.out.println("╠══════════════════════════════════════╣");
                System.out.println("║  Phone: " + phoneNumber);
                System.out.println("║  OTP:   " + otp);
                System.out.println("║  Valid for: " + otpExpiryMinutes + " minutes");
                System.out.println("╚══════════════════════════════════════╝");
            } else {
                // PRODUCTION MODE: Send via Twilio/WhatsApp
                sendOtpViaSms(phoneNumber, otp);
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("Failed to send OTP: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        String otpKey = OTP_KEY_PREFIX + phoneNumber;
        String attemptsKey = ATTEMPTS_KEY_PREFIX + phoneNumber;
        
        // Get stored OTP
        Object storedOtp = redisTemplate.opsForValue().get(otpKey);
        
        if (storedOtp == null) {
            System.out.println("OTP expired or not found for: " + phoneNumber);
            return false;
        }
        
        // Get current attempts
        Object attemptsObj = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsObj != null ? Integer.parseInt(attemptsObj.toString()) : 0;
        
        if (attempts >= maxAttempts) {
            System.out.println("Max OTP attempts exceeded for: " + phoneNumber);
            // Delete OTP to prevent further attempts
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);
            return false;
        }
        
        // Verify OTP
        if (storedOtp.toString().equals(otp)) {
            System.out.println("OTP verified successfully for: " + phoneNumber);
            // Delete OTP after successful verification
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);
            return true;
        } else {
            // Increment attempts
            redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1), 
                otpExpiryMinutes, TimeUnit.MINUTES);
            System.out.println("Invalid OTP. Attempts: " + (attempts + 1) + "/" + maxAttempts);
            return false;
        }
    }
    
    @Override
    public int getRemainingAttempts(String phoneNumber) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX + phoneNumber;
        Object attemptsObj = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsObj != null ? Integer.parseInt(attemptsObj.toString()) : 0;
        return Math.max(0, maxAttempts - attempts);
    }
    
    // Helper methods
    
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    private void sendOtpViaSms(String phoneNumber, String otp) {
        // TODO: Integrate with Twilio or other SMS provider
        // For now, this is a placeholder
        
        /*
        // Example Twilio integration:
        
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        
        Message message = Message.creator(
            new PhoneNumber(phoneNumber),  // To
            new PhoneNumber(TWILIO_PHONE), // From
            "Your OTP is: " + otp + ". Valid for " + otpExpiryMinutes + " minutes."
        ).create();
        
        System.out.println("SMS sent via Twilio: " + message.getSid());
        */
        
        System.out.println("SMS would be sent to: " + phoneNumber + " with OTP: " + otp);
    }
}

