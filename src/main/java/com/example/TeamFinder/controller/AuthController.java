package com.example.TeamFinder.controller;

import com.example.TeamFinder.dto.OtpRequest;
import com.example.TeamFinder.dto.OtpVerificationRequest;
import com.example.TeamFinder.service.EmailService;
import com.example.TeamFinder.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/otp")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        try {
            // 1. Generate OTP
            String otp = otpService.generateAndStoreOtp(request.getEmail());

            // 2. Send Email
            emailService.sendOtpEmail(request.getEmail(), otp);

            return new ResponseEntity<>("OTP sent successfully to " + request.getEmail(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to send OTP email.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());

        if (isValid) {
            return new ResponseEntity<>("OTP verified successfully.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid or expired OTP.", HttpStatus.BAD_REQUEST);
        }
    }
}