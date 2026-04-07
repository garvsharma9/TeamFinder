package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.LoginRequest;
import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.UserRepository;
import com.example.TeamFinder.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.*;

@Slf4j
@Service
public class UserService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;
    @Autowired
    private EmailService emailService;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    @Value("${google.client.id}")
    private String googleClientId;
// ... inside your UserService class:

    // Replace with your actual Client ID from Google Cloud Console
//    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";
    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    public User verifyGithubCodeAndGetUser(String code) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Exchange code for Access Token
        String tokenUrl = "https://github.com/login/oauth/access_token";
        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("client_id", githubClientId);
        tokenParams.put("client_secret", githubClientSecret);
        tokenParams.put("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        if (accessToken == null) throw new Exception("Failed to retrieve GitHub access token");

        // 2. Fetch User Profile
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        HttpEntity<String> authEntity = new HttpEntity<>(authHeaders);

        ResponseEntity<Map> profileResponse = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET, authEntity, Map.class);
        Map<String, Object> profile = profileResponse.getBody();

        String name = (String) profile.get("name");
        String avatarUrl = (String) profile.get("avatar_url");
        String githubProfileUrl = (String) profile.get("html_url");

        // 3. Fetch User Emails (GitHub requires a separate call for emails if they are private)
        ResponseEntity<List> emailsResponse = restTemplate.exchange("https://api.github.com/user/emails", HttpMethod.GET, authEntity, List.class);
        List<Map<String, Object>> emails = emailsResponse.getBody();

        String primaryEmail = null;
        for (Map<String, Object> emailObj : emails) {
            if ((Boolean) emailObj.get("primary") && (Boolean) emailObj.get("verified")) {
                primaryEmail = (String) emailObj.get("email");
                break;
            }
        }

        if (primaryEmail == null) throw new Exception("No verified primary email found on GitHub");

        // 4. SMART MERGE LOGIC
        Optional<User> existingUserOpt = userRepository.findFirstByEmail(primaryEmail);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            // If they don't have a GitHub URL yet, attach this one!
            if (existingUser.getGithubUrl() == null || existingUser.getGithubUrl().isEmpty()) {
                existingUser.setGithubUrl(githubProfileUrl);
                return userRepository.save(existingUser);
            }
            return existingUser;
        } else {
            // User doesn't exist, create a new profile
            User newUser = new User();
            newUser.setEmail(primaryEmail);
            newUser.setName(name != null ? name : "GitHub User");
            newUser.setProfilePictureUrl(avatarUrl);
            newUser.setGithubUrl(githubProfileUrl);

            // Generate unique username
            String baseUsername = primaryEmail.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
            String uniqueUsername = baseUsername;
            int suffix = 1;
            while (userRepository.findByUsername(uniqueUsername).isPresent()) {
                uniqueUsername = baseUsername + suffix;
                suffix++;
            }
            newUser.setUsername(uniqueUsername);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password

            return userRepository.save(newUser);
        }
    }

    public User verifyGoogleTokenAndGetUser(String idTokenString) throws Exception {
        System.out.println("BACKEND EXPECTED CLIENT ID: [" + googleClientId + "]");
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Check if user already exists by EMAIL
            Optional<User> existingUser = userRepository.findFirstByEmail(email);

            if (existingUser.isPresent()) {
                return existingUser.get(); // User exists, log them in
            } else {
                // User doesn't exist, create a new profile
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                newUser.setProfilePictureUrl(pictureUrl);

                // Generate a unique username from their email prefix
                String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
                String uniqueUsername = baseUsername;
                int suffix = 1;
                while (userRepository.findByUsername(uniqueUsername).isPresent()) {
                    uniqueUsername = baseUsername + suffix;
                    suffix++;
                }
                newUser.setUsername(uniqueUsername);

                // Give them a random password since they use Google to log in
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

                return userRepository.save(newUser);
            }
        } else {
            throw new Exception("Invalid Google ID token.");
        }
    }
    // get user profile for dashboard rendering
    public ResponseEntity<?> getUserProfile(String id) {
        Optional<User> byUsername = userRepository.findByUsername(id);
        return new ResponseEntity<>(byUsername, HttpStatusCode.valueOf(200));
    }

    public boolean signup(User user) {
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(user.getPassword())));
        user.setRoles(Arrays.asList("USER"));
        userRepository.insert(user);
        return true;
    }

    public boolean saveAdmin(User user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER", "ADMIN"));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("Error", e);
            return false;
        }
    }

    // Method to create a Club President
    public boolean createPresident(User user) {
        try {
            // Check if username already exists to prevent overriding
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // Give them both roles so they can still act like a normal user if they want
            user.setRoles(Arrays.asList("USER", "PRESIDENT"));

            userRepository.save(user);
            return true;
        } catch (Exception e) {
            log.error("Error creating president", e);
            return false;
        }
    }
    // Make sure to autowire JwtUtil at the top of your UserService:


    // Update your signin method:
    public ResponseEntity<?> signin(LoginRequest loginRequest) {
        Optional<User> byUsername = userRepository.findByUsername(loginRequest.getUsername());
        if (byUsername.isPresent()) {
            if (passwordEncoder.matches(loginRequest.getPassword(), byUsername.get().getPassword())) {

                // 1. Generate the token!
                String token = jwtUtil.generateToken(loginRequest.getUsername());

                // 2. We need to send both the User AND the token back to React.
                // An easy way is to use a Map:
                Map<String, Object> response = new HashMap<>();
                response.put("user", byUsername.get());
                response.put("token", token);

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public boolean changeBio(User user) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            byUsername.get().setBio(user.getBio());
            userRepository.save(byUsername.get()); // FIXED: Changed from insert to save
            return true;
        }
        return false;
    }

    public boolean addEmail(User user) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            byUsername.get().setEmail(user.getEmail());
            userRepository.save(byUsername.get());
            return true;
        }
        return false;
    }

    public boolean deleteEmail(User user) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            byUsername.get().setEmail("");
            userRepository.save(byUsername.get());
            return true;
        }
        return false;
    }

    public boolean addCollege(User user) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            byUsername.get().setCollege(user.getCollege());
            userRepository.save(byUsername.get());
            return true;
        }
        return false;
    }

    public boolean deleteCollege(User user) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            byUsername.get().setCollege("");
            userRepository.save(byUsername.get());
            return true;
        }
        return false;
    }

    public List<User> searchUserByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<User> searchUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> searchUserBySkill(String skill) {
        return userRepository.findBySkillContainingIgnoreCase(skill);
    }

    public boolean addSkill(User user) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            User existingUser = byUsername.get();

            // FIXED: Prevent NullPointerException if skills list is completely empty
            if (existingUser.getSkill() == null) {
                existingUser.setSkill(new ArrayList<>());
            }

            // Loop through new skills and add them (preventing duplicates)
            if (user.getSkill() != null) {
                for (String skill : user.getSkill()) {
                    if (!existingUser.getSkill().contains(skill)) {
                        existingUser.getSkill().add(skill);
                    }
                }
            }

            userRepository.save(existingUser);
            return true;
        }
        return false;
    }

    public boolean removeSkill(User user, String skillToRemove) {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if (byUsername.isPresent()) {
            User existingUser = byUsername.get();

            // FIXED: Prevent NullPointerException
            if (existingUser.getSkill() != null) {
                existingUser.getSkill().remove(skillToRemove);
                userRepository.save(existingUser);
            }
            return true;
        }
        return false;
    }


    // Replace your existing likeUser method in UserService.java
    public boolean likeUser(String targetUsername, String likerUsername) {
        Optional<User> targetUserOpt = userRepository.findByUsername(targetUsername);

        if (targetUserOpt.isPresent()) {
            User targetUser = targetUserOpt.get();

            // 1. Prevent NullPointerException if the list is completely empty in the DB
            if (targetUser.getLikedBy() == null) {
                targetUser.setLikedBy(new ArrayList<>());
            }

            // 2. CHECK: Has this person already liked the profile?
            if (targetUser.getLikedBy().contains(likerUsername)) {
                return false; // Stop! They already liked it.
            }

            // 3. Add the liker's username to the list
            targetUser.getLikedBy().add(likerUsername);

            // 4. Increment likes
            targetUser.setLikesReceived(targetUser.getLikesReceived() + 1);

            // 5. Update experience tag
            if (targetUser.getLikesReceived() >= 10) {
                targetUser.setExperienceTag("Pro");
            } else if (targetUser.getLikesReceived() >= 5) {
                targetUser.setExperienceTag("Intermediate");
            }

            userRepository.save(targetUser);
            return true;
        }
        return false;
    }
    // Change return type from boolean to User
    public User updateUserProfile(String username, User updatedData) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();

            if (updatedData.getName() != null) existingUser.setName(updatedData.getName());
            if (updatedData.getBio() != null) existingUser.setBio(updatedData.getBio());
            if (updatedData.getBranch() != null) existingUser.setBranch(updatedData.getBranch());
            if (updatedData.getCollege() != null) existingUser.setCollege(updatedData.getCollege());

            // These are correct!
            if (updatedData.getLinkedinUrl() != null) existingUser.setLinkedinUrl(updatedData.getLinkedinUrl());
            if (updatedData.getGithubUrl() != null) existingUser.setGithubUrl(updatedData.getGithubUrl());

            if (updatedData.getSkill() != null) {
                existingUser.setSkill(normalizeSkills(updatedData.getSkill()));
            }

            // Save and RETURN the updated entity
            return userRepository.save(existingUser);
        }
        // Return null if user isn't found
        return null;
    }

    public ResponseEntity<?> initiateLogin(LoginRequest loginRequest) {
        Optional<User> byUsername = userRepository.findByUsername(loginRequest.getUsername());

        if (byUsername.isPresent()) {
            User user = byUsername.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

                // Check if user has an email registered
                if (user.getEmail() == null || user.getEmail().isEmpty()) {
                    return new ResponseEntity<>("No email registered for this account.", HttpStatus.BAD_REQUEST);
                }

                // Generate and send OTP
                String otp = otpService.generateAndStoreOtp(user.getEmail());
                emailService.sendOtpEmail(user.getEmail(), otp);

                // Return success, but DO NOT send the JWT token yet!
                Map<String, String> response = new HashMap<>();
                response.put("message", "OTP sent successfully");
                response.put("email", user.getEmail()); // Send masked email to React if you want

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid password", HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> verifyLoginOtp(String username, String otp) {
        Optional<User> byUsername = userRepository.findByUsername(username);

        if (byUsername.isPresent()) {
            User user = byUsername.get();

            // Validate the OTP against the user's email
            boolean isValid = otpService.validateOtp(user.getEmail(), otp);

            if (isValid) {
                // Generate the JWT Token!
                String token = jwtUtil.generateToken(user.getUsername());

                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                response.put("token", token);

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
    }

    // Add these imports if you don't have them
    // import org.springframework.security.crypto.password.PasswordEncoder;
    // import java.util.Map;

    public String requestPasswordReset(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String email = user.getEmail();
        String otp = otpService.generateAndStoreOtp(email);

        // Ensure you call your email service here to actually send the OTP!
        emailService.sendOtpEmail(email, otp);

        // Return masked email (e.g., g***9@gmail.com) for the UI
        return email.replaceAll("(^[^@]{1,3})[^@]*(@.*$)", "$1***$2");
    }

    public void resetPassword(String username, String otp, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // This uses your existing in-memory ConcurrentHashMap logic!
        boolean isValid = otpService.validateOtp(user.getEmail(), otp);
        if (!isValid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Encode and save the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    @Autowired
    private CloudinaryService cloudinaryService;

    public String updateProfilePicture(String username, MultipartFile file) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(file);
        user.setProfilePictureUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    public String updateBannerPicture(String username, MultipartFile file) throws Exception {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(file);
        user.setBannerPictureUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }


//    public void sendConnectionRequest(String senderUsername, String targetUsername) {
//        if (senderUsername.equals(targetUsername)) throw new RuntimeException("Cannot connect with yourself");
//
//        User sender = userRepository.findByUsername(senderUsername).orElseThrow();
//        User target = userRepository.findByUsername(targetUsername).orElseThrow();
//
//        // Safety check for null lists
//        if (sender.getConnectionRequestsSent() == null) sender.setConnectionRequestsSent(new ArrayList<>());
//        if (sender.getConnectionRequestsReceived() == null) sender.setConnectionRequestsReceived(new ArrayList<>());
//        if (target.getConnectionRequestsSent() == null) target.setConnectionRequestsSent(new ArrayList<>());
//        if (target.getConnectionRequestsReceived() == null) target.setConnectionRequestsReceived(new ArrayList<>());
//        if (sender.getConnections() == null) sender.setConnections(new ArrayList<>());
//        if (target.getConnections() == null) target.setConnections(new ArrayList<>());
//
//        // Don't send if already connected or pending
//        if (
//                sender.getConnections().contains(targetUsername) ||
//                        target.getConnections().contains(senderUsername) ||
//                        sender.getConnectionRequestsSent().contains(targetUsername)
//        ) {
//            return;
//        }
//
//        if (
//                sender.getConnectionRequestsReceived().contains(targetUsername) ||
//                        target.getConnectionRequestsSent().contains(senderUsername)
//        ) {
//            throw new RuntimeException("This user has already sent you a connection request");
//        }
//
//        sender.getConnectionRequestsSent().add(targetUsername);
//        target.getConnectionRequestsReceived().add(senderUsername);
//
//        userRepository.save(sender);
//        userRepository.save(target);
//    }

    public void sendConnectionRequest(String senderUsername, String targetUsername) {
        if (senderUsername.equals(targetUsername)) throw new RuntimeException("Cannot connect with yourself");

        User sender = userRepository.findByUsername(senderUsername).orElseThrow();
        User target = userRepository.findByUsername(targetUsername).orElseThrow();

        // Safety check for null lists
        if (sender.getConnectionRequestsSent() == null) sender.setConnectionRequestsSent(new ArrayList<>());
        if (sender.getConnectionRequestsReceived() == null) sender.setConnectionRequestsReceived(new ArrayList<>());
        if (target.getConnectionRequestsSent() == null) target.setConnectionRequestsSent(new ArrayList<>());
        if (target.getConnectionRequestsReceived() == null) target.setConnectionRequestsReceived(new ArrayList<>());
        if (sender.getConnections() == null) sender.setConnections(new ArrayList<>());
        if (target.getConnections() == null) target.setConnections(new ArrayList<>());

        // Don't send if already connected or pending
        if (
                sender.getConnections().contains(targetUsername) ||
                        target.getConnections().contains(senderUsername) ||
                        sender.getConnectionRequestsSent().contains(targetUsername)
        ) {
            return;
        }

        if (
                sender.getConnectionRequestsReceived().contains(targetUsername) ||
                        target.getConnectionRequestsSent().contains(senderUsername)
        ) {
            throw new RuntimeException("This user has already sent you a connection request");
        }

        sender.getConnectionRequestsSent().add(targetUsername);
        target.getConnectionRequestsReceived().add(senderUsername);

        userRepository.save(sender);
        userRepository.save(target);

        // --- NEW: OFFLINE NOTIFICATION LOGIC ---
        // Check if target user is offline AND has a valid email address
        if (!target.isOnline() && target.getEmail() != null && !target.getEmail().isEmpty()) {

            // Get their display name, fallback to username if name isn't set
            String receiverName = target.getName() != null ? target.getName() : target.getUsername();

            // Trigger the background email using your EmailService
            emailService.sendOfflineConnectionEmail(target.getEmail(), receiverName, senderUsername);
        }
    }

    public void acceptConnectionRequest(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();
        User sender = userRepository.findByUsername(senderUsername).orElseThrow();

        // Initialize connection lists
        if (receiver.getConnections() == null) receiver.setConnections(new ArrayList<>());
        if (sender.getConnections() == null) sender.setConnections(new ArrayList<>());
        if (receiver.getConnectionRequestsSent() == null) receiver.setConnectionRequestsSent(new ArrayList<>());
        if (receiver.getConnectionRequestsReceived() == null) receiver.setConnectionRequestsReceived(new ArrayList<>());
        if (sender.getConnectionRequestsSent() == null) sender.setConnectionRequestsSent(new ArrayList<>());
        if (sender.getConnectionRequestsReceived() == null) sender.setConnectionRequestsReceived(new ArrayList<>());

        // Add to each other's connections
        addUnique(receiver.getConnections(), senderUsername);
        addUnique(sender.getConnections(), receiverUsername);

        // Remove pending requests in both directions so the relationship stays clean.
        removeValue(receiver.getConnectionRequestsReceived(), senderUsername);
        removeValue(sender.getConnectionRequestsSent(), receiverUsername);
        removeValue(receiver.getConnectionRequestsSent(), senderUsername);
        removeValue(sender.getConnectionRequestsReceived(), receiverUsername);

        userRepository.save(receiver);
        userRepository.save(sender);
    }

    public void rejectConnectionRequest(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();
        User sender = userRepository.findByUsername(senderUsername).orElseThrow();

        if (receiver.getConnectionRequestsSent() == null) receiver.setConnectionRequestsSent(new ArrayList<>());
        if (receiver.getConnectionRequestsReceived() == null) receiver.setConnectionRequestsReceived(new ArrayList<>());
        if (sender.getConnectionRequestsSent() == null) sender.setConnectionRequestsSent(new ArrayList<>());
        if (sender.getConnectionRequestsReceived() == null) sender.setConnectionRequestsReceived(new ArrayList<>());

        // Just remove from pending requests without connecting
        removeValue(receiver.getConnectionRequestsReceived(), senderUsername);
        removeValue(sender.getConnectionRequestsSent(), receiverUsername);
        removeValue(receiver.getConnectionRequestsSent(), senderUsername);
        removeValue(sender.getConnectionRequestsReceived(), receiverUsername);

        userRepository.save(receiver);
        userRepository.save(sender);
    }

    public void removeConnection(String username, String targetUsername) {
        if (username.equals(targetUsername)) throw new RuntimeException("Cannot remove yourself");

        User user = userRepository.findByUsername(username).orElseThrow();
        User target = userRepository.findByUsername(targetUsername).orElseThrow();

        if (user.getConnections() == null) user.setConnections(new ArrayList<>());
        if (target.getConnections() == null) target.setConnections(new ArrayList<>());
        if (user.getConnectionRequestsSent() == null) user.setConnectionRequestsSent(new ArrayList<>());
        if (user.getConnectionRequestsReceived() == null) user.setConnectionRequestsReceived(new ArrayList<>());
        if (target.getConnectionRequestsSent() == null) target.setConnectionRequestsSent(new ArrayList<>());
        if (target.getConnectionRequestsReceived() == null) target.setConnectionRequestsReceived(new ArrayList<>());

        removeValue(user.getConnections(), targetUsername);
        removeValue(target.getConnections(), username);
        removeValue(user.getConnectionRequestsSent(), targetUsername);
        removeValue(user.getConnectionRequestsReceived(), targetUsername);
        removeValue(target.getConnectionRequestsSent(), username);
        removeValue(target.getConnectionRequestsReceived(), username);

        userRepository.save(user);
        userRepository.save(target);
    }

    private void addUnique(List<String> values, String value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }

    private void removeValue(List<String> values, String value) {
        values.removeIf(value::equals);
    }

    public List<User> getPendingRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> requesters = user.getConnectionRequestsReceived();
        List<User> pendingUsers = new ArrayList<>();

        if (requesters != null) {
            for (String reqUsername : requesters) {
                userRepository.findByUsername(reqUsername).ifPresent(u -> {
                    u.setPassword(null); // NEVER send passwords to the frontend!
//                    u.setResetOtp(null);
                    pendingUsers.add(u);
                });
            }
        }
        return pendingUsers;
    }

    public List<User> getMyConnections(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> connections = user.getConnections();
        List<User> connectedUsers = new ArrayList<>();

        if (connections != null) {
            for (String connUsername : connections) {
                userRepository.findByUsername(connUsername).ifPresent(u -> {
                    u.setPassword(null); // Keep it secure
//                    u.setResetOtp(null);
                    connectedUsers.add(u);
                });
            }
        }
        return connectedUsers;
    }

    private List<String> normalizeSkills(List<String> skills) {
        if (skills == null) return new ArrayList<>();
        return skills.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .distinct() // Removes duplicates like "React" and "react"
                .toList();
    }
}