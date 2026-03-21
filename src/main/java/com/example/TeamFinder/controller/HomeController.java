package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/search-by-username/{username}")
    public ResponseEntity<?> searchUserByUsername(@PathVariable String username) {
        // Changed to expect a List instead of Optional, since "ga" could match "garv", "gaurav", etc.
        Optional<User> users = userService.searchUserByUsername(username);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/search-by-name/{name}")
    public ResponseEntity<?> searchByName(@PathVariable String name) {
        List<User> users = userService.searchUserByName(name);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/search-by-skill/{skill}")
    public ResponseEntity<?> searchBySkill(@PathVariable String skill) {
        List<User> users = userService.searchUserBySkill(skill);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PostMapping("/like/{targetUsername}")
    public ResponseEntity<?> likeUserProfile(
            @PathVariable String targetUsername,
            @RequestParam String likerUsername) {

        boolean success = userService.likeUser(targetUsername, likerUsername);

        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("User already liked this profile", HttpStatus.BAD_REQUEST);
    }
}