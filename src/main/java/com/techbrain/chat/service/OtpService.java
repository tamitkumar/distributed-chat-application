package com.techbrain.chat.service;

public interface OtpService {
    
    /**
     * Send OTP to phone number
     * @param phoneNumber Phone number (e.g., +919876543210)
     * @return true if OTP sent successfully
     */
    boolean sendOtp(String phoneNumber);
    
    /**
     * Verify OTP
     * @param phoneNumber Phone number
     * @param otp OTP code
     * @return true if OTP is valid
     */
    boolean verifyOtp(String phoneNumber, String otp);
    
    /**
     * Get remaining attempts for phone number
     */
    int getRemainingAttempts(String phoneNumber);
}

