package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // --- NEW UNIFIED UPDATE ENDPOINT ---
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUserData) {
        try {
            // 1. Get the currently logged-in user's username directly from the JWT Token!
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // 2. Pass it to the service to update
            boolean success = userService.updateUserProfile(currentUsername, updatedUserData);

            if (success) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update profile.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // --- YOUR EXISTING ENDPOINTS ---

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable String id) {
        return userService.getUserProfile(id);
    }

    @PutMapping("/change-bio")
    public ResponseEntity<?> changeBio(@RequestBody User user) {
        boolean b = userService.changeBio(user);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/add-email")
    public ResponseEntity<?> addEmail(@RequestBody User user) {
        boolean b = userService.addEmail(user);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete-email")
    public ResponseEntity<?> deleteEmail(@RequestBody User user) {
        boolean b = userService.deleteEmail(user);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/add-college")
    public ResponseEntity<?> addCollege(@RequestBody User user) {
        boolean b = userService.addCollege(user);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/delete-college")
    public ResponseEntity<?> deleteCollege(@RequestBody User user) {
        boolean b = userService.deleteCollege(user);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/add-skill")
    public ResponseEntity<?> addSkill(@RequestBody User user) {
        boolean b = userService.addSkill(user);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/delete-skill/{skill}")
    public ResponseEntity<?> deleteSkill(@RequestBody User user, @PathVariable String skill) {
        boolean b = userService.removeSkill(user, skill);
        if(b) return new ResponseEntity<>(HttpStatus.OK);
        else return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}