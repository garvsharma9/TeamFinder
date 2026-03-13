package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.LoginRequest;
import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.service.PublicService;
import com.example.TeamFinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private PublicService publicService;
    @Autowired
    private UserService userService;
    @GetMapping("/health-check")
    public ResponseEntity<?> heathCheck()
    {
        return new ResponseEntity<>(publicService.healthCheck(), HttpStatusCode.valueOf(200));
    }
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody User user)
    {
        userService.signup(user);
        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }
//    @PostMapping("/signin")
//    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest)
//    {
//
//        ResponseEntity<?> signin = userService.signin(loginRequest);
//        return signin;
//    }
    // 1. This catches the initial username/password from React
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
        // Calls the new method we just added to UserService
        return userService.initiateLogin(loginRequest);
    }

    // 2. This catches the 6-digit OTP from React
    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");

        // Calls the second new method we added to UserService
        return userService.verifyLoginOtp(username, otp);
    }


    @PostMapping("/forgot-password/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> payload) {
        try {
            String maskedEmail = userService.requestPasswordReset(payload.get("username"));
            return ResponseEntity.ok(Map.of("email", maskedEmail));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        try {
            userService.resetPassword(
                    payload.get("username"),
                    payload.get("otp"),
                    payload.get("newPassword")
            );
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
