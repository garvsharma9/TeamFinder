package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.LoginRequest;
import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.service.PublicService;
import com.example.TeamFinder.service.UserService;
import com.example.TeamFinder.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/public")
@Slf4j
public class PublicController {

    @Autowired
    private PublicService publicService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @GetMapping("/health-check")
    public ResponseEntity<?> heathCheck()
    {
        return new ResponseEntity<>(publicService.healthCheck(), HttpStatusCode.valueOf(200));
    }
    @PostMapping("/auth/github")
    public ResponseEntity<?> githubAuth(@RequestBody Map<String, String> payload) {
        try {
            String code = payload.get("code");
            User user = userService.verifyGithubCodeAndGetUser(code);
            String appJwtToken = jwtUtil.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", appJwtToken);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("GitHub Authentication failed", HttpStatus.UNAUTHORIZED);
        }
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
    @PostMapping("/auth/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> payload) {
        try {
            String googleToken = payload.get("token");

            // 1. Verify token and get/create user
            User user = userService.verifyGoogleTokenAndGetUser(googleToken);

            // 2. Generate YOUR app's standard JWT token for this user
            String appJwtToken = jwtUtil.generateToken(user.getUsername());

            // 3. Return both to the frontend, just like a normal login
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", appJwtToken);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Google Auth Failed", e);
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
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

    // Add this inside PublicController.java
    @GetMapping("/github/{username}/repos")
    public ResponseEntity<?> getCachedGithubRepos(@PathVariable String username) {
        List<?> repos = publicService.getGithubRepos(username);

        if (repos != null) {
            return ResponseEntity.ok(repos);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("GitHub data not found or rate limited");
        }
    }
}
