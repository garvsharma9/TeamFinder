package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.UserRepository;
import com.example.TeamFinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
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

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            String username = authentication.getName();
            String imageUrl = userService.updateProfilePicture(username, file);
            return ResponseEntity.ok(Map.of("message", "Avatar updated successfully", "url", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
        }
    }

    @PostMapping("/upload-banner")
    public ResponseEntity<?> uploadBanner(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            String username = authentication.getName();
            String imageUrl = userService.updateBannerPicture(username, file);
            return ResponseEntity.ok(Map.of("message", "Banner updated successfully", "url", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
        }
    }
    @PostMapping("/connect/request/{targetUsername}")
    public ResponseEntity<?> requestConnection(@PathVariable String targetUsername, Authentication auth) {
        try {
            userService.sendConnectionRequest(auth.getName(), targetUsername);
            return ResponseEntity.ok("Request sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/connect/accept/{senderUsername}")
    public ResponseEntity<?> acceptConnection(@PathVariable String senderUsername, Authentication auth) {
        try {
            userService.acceptConnectionRequest(auth.getName(), senderUsername);
            return ResponseEntity.ok("Request accepted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/connect/reject/{senderUsername}")
    public ResponseEntity<?> rejectConnection(@PathVariable String senderUsername, Authentication auth) {
        try {
            userService.rejectConnectionRequest(auth.getName(), senderUsername);
            return ResponseEntity.ok("Request rejected");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/connect/remove/{targetUsername}")
    public ResponseEntity<?> removeConnection(@PathVariable String targetUsername, Authentication auth) {
        try {
            userService.removeConnection(auth.getName(), targetUsername);
            return ResponseEntity.ok("Connection removed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/connect/pending")
    public ResponseEntity<?> getPendingRequests(Authentication auth) {
        try {
            return ResponseEntity.ok(userService.getPendingRequests(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/connect/accepted")
    public ResponseEntity<?> getMyConnections(Authentication auth) {
        try {
            return ResponseEntity.ok(userService.getMyConnections(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/status")
    public ResponseEntity<?> setOnlineStatus(@RequestParam boolean online, Authentication auth) {
        try {
            User user = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setOnline(online);
            userRepository.save(user);
            return ResponseEntity.ok("Status updated to: " + (online ? "Online" : "Offline"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update status");
        }
    }
}