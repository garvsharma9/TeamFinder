package com.example.TeamFinder.service;

import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // Stores Email -> OTP
    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();

    // Stores Email -> Expiration Time (in milliseconds)
    private final ConcurrentHashMap<String, Long> otpExpiration = new ConcurrentHashMap<>();

    // 5 minutes in milliseconds
    private final long OTP_VALID_DURATION = 5 * 60 * 1000;

    public String generateAndStoreOtp(String email) {
        // Generate a 6-digit random number
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store the OTP and its expiration time
        otpStorage.put(email, otp);
        otpExpiration.put(email, System.currentTimeMillis() + OTP_VALID_DURATION);

        return otp;
    }

    public boolean validateOtp(String email, String submittedOtp) {
        if (!otpStorage.containsKey(email)) {
            return false; // OTP not requested or already used
        }

        long expirationTime = otpExpiration.get(email);
        if (System.currentTimeMillis() > expirationTime) {
            // OTP expired, clean it up
            clearOtp(email);
            return false;
        }

        String storedOtp = otpStorage.get(email);
        if (storedOtp.equals(submittedOtp)) {
            // Success! Clean up the OTP so it can't be reused
            clearOtp(email);
            return true;
        }

        return false;
    }

    public void clearOtp(String email) {
        otpStorage.remove(email);
        otpExpiration.remove(email);
    }
}