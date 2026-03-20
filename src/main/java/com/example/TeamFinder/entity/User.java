package com.example.TeamFinder.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("user")
@Data
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String username;
    private String password;
    private String name;
    private String email;
    private String college;
    private String bio;
    private String branch;
    @Indexed
    private List<String> skill;
    private List<String> roles;
    private List<String> posts;
    private String experienceTag; // "Beginner", "Intermediate", "Pro"
    private int likesReceived; // Increases when a team leader likes their profile
    // --- Online Status ---
    private boolean isOnline;
    // Add this new field to your User.java class
    private List<String> likedBy;
    private String profilePictureUrl;
    private String bannerPictureUrl;
    // --- Connection Tracking ---
    private List<String> connections; // People who have mutually accepted
    private List<String> connectionRequestsSent; // People I want to connect with
    private List<String> connectionRequestsReceived; // People who want to connect with me
    private String linkedinUrl;
    private String githubUrl;
// Make sure to add the Getter and Setter for it if you aren't using Lombok's @Data!
}
