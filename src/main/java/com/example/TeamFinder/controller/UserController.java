package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.UserRepository;
import com.example.TeamFinder.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    // --- NEW UNIFIED UPDATE ENDPOINT ---
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUserData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // 1. Catch the updated user object from the service
            User updatedUser = userService.updateUserProfile(currentUsername, updatedUserData);

            if (updatedUser != null) {
                // 2. Return the user object inside the 200 OK response!
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
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
    @PostMapping("/users/batch")
    public ResponseEntity<?> getUsersBatch(@RequestBody Map<String, List<String>> payload) {
        try {
            List<String> usernames = payload.get("usernames");
            if (usernames == null || usernames.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Call the repository method we just made
            List<User> users = userRepository.findByUsernameIn(usernames);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch users");
        }
    }

    @GetMapping("/suggested-teammates")
    public ResponseEntity<?> getSuggestedTeammates() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
            if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            // 1. Get current user's skills and lowercase them for the mapping logic
            List<String> userSkills = currentUser.getSkill() != null
                    ? currentUser.getSkill().stream().map(String::toLowerCase).toList()
                    : new ArrayList<>();

            // Fallback: If no skills, just return top 10 others
            if (userSkills.isEmpty()) {
                List<User> genericSuggestions = userRepository.findTop10ByUsernameNot(currentUsername);
                return ResponseEntity.ok(genericSuggestions);
            }

            // 2. THE ALGORITHM: Build a set of lowercase target skills
            Set<String> targetSkills = new HashSet<>();
            for (String s : userSkills) {
                // Frontend -> Backend
                if (s.contains("react") || s.contains("vue") || s.contains("angular") || s.contains("frontend") || s.contains("html")) {
                    targetSkills.addAll(Arrays.asList("spring boot", "node.js", "express", "django", "java", "python", "mongodb", "postgresql"));
                }
                // Backend -> Frontend/UI
                else if (s.contains("spring") || s.contains("node") || s.contains("java") || s.contains("backend") || s.contains("python")) {
                    targetSkills.addAll(Arrays.asList("react", "vue", "angular", "ui/ux", "figma", "frontend", "tailwind"));
                }
                // Designer -> Developers
                else if (s.contains("ui") || s.contains("ux") || s.contains("figma") || s.contains("design")) {
                    targetSkills.addAll(Arrays.asList("react", "vue", "flutter", "swift", "android", "frontend"));
                }
                // Mobile -> Backend
                else if (s.contains("flutter") || s.contains("react native") || s.contains("android") || s.contains("ios") || s.contains("swift")) {
                    targetSkills.addAll(Arrays.asList("firebase", "node.js", "spring boot", "aws", "python"));
                }
                // Data Science -> Visualization/DevOps
                else if (s.contains("data") || s.contains("ml") || s.contains("ai") || s.contains("machine learning")) {
                    targetSkills.addAll(Arrays.asList("python", "aws", "docker", "flask", "fastapi", "react"));
                }
            }

            // If no matches found in our map, suggest popular complementary stacks
            if (targetSkills.isEmpty()) {
                targetSkills.addAll(Arrays.asList("react", "spring boot", "node.js", "python", "ui/ux"));
            }

            // 3. Query the lowercase database for the target skills
            List<User> suggestedUsers = userRepository.findDistinctBySkillInAndUsernameNot(new ArrayList<>(targetSkills), currentUsername);

            // 4. FILLER LOGIC: If we found very few people, add some random users so the carousel looks full
            if (suggestedUsers.size() < 6) {
                List<User> fillers = userRepository.findTop10ByUsernameNot(currentUsername);
                for (User filler : fillers) {
                    // Add the filler only if they aren't already in the suggestion list
                    if (suggestedUsers.stream().noneMatch(u -> u.getUsername().equals(filler.getUsername()))) {
                        suggestedUsers.add(filler);
                    }
                    if (suggestedUsers.size() >= 10) break;
                }
            }

            // 5. Shuffle for freshness and limit to 10
            Collections.shuffle(suggestedUsers);
            List<User> finalSuggestions = suggestedUsers.stream().limit(10).toList();

            return ResponseEntity.ok(finalSuggestions);

        } catch (Exception e) {
            log.error("Error in suggestion algorithm: ", e);
            return ResponseEntity.internalServerError().body("Error generating suggestions");
        }
    }
}