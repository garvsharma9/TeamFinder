package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.Post;
import com.example.TeamFinder.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/post")
@RestController
@Slf4j
public class PostEntryController {

    @Autowired
    private PostService postService;

    @PostMapping("/add-post")
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        try {
            boolean success = postService.savePost(post);
            if (success) return new ResponseEntity<>(HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while creating post ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    // Changed to PathVariable for standard DELETE structure
//    @DeleteMapping("/delete-post/{postId}")
//    public ResponseEntity<?> deletePost(@PathVariable String postId) {
//        boolean success = postService.deletePost(postId);
//        if (success) return new ResponseEntity<>(HttpStatus.OK);
//        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//    }

    // Changed to PathVariable (URLs like /post/like/12345 are much cleaner)
    @PutMapping("/like/{postId}")
    public ResponseEntity<?> like(@PathVariable String postId) {
        boolean success = postService.like(postId);
        if (success) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/dislike/{postId}")
    public ResponseEntity<?> dislike(@PathVariable String postId) {
        boolean success = postService.dislike(postId);
        if (success) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ... (Keep your existing endpoints) ...

    @GetMapping("/all")
    public ResponseEntity<?> getAllPosts() {
        return new ResponseEntity<>(postService.getAllPosts(), HttpStatus.OK);
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeedPosts() {
        return new ResponseEntity<>(postService.getFeedPosts(), HttpStatus.OK);
    }

    // A user clicks "Request to Join" on the frontend
    @PostMapping("/{postId}/request")
    public ResponseEntity<?> requestToJoin(@PathVariable String postId, @RequestParam String requesterUsername) {
        boolean success = postService.requestToJoin(postId, requesterUsername);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid request or already requested", HttpStatus.BAD_REQUEST);
    }

    // The post owner clicks "Accept" on a user's request
    @PostMapping("/{postId}/accept")
    public ResponseEntity<?> acceptRequest(
            @PathVariable String postId,
            @RequestParam String ownerUsername,
            @RequestParam String targetUsername) {

        boolean success = postService.acceptRequest(postId, ownerUsername, targetUsername);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Action not allowed", HttpStatus.FORBIDDEN);
    }

    // The post owner clicks "Reject" on a user's request
    @PostMapping("/{postId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable String postId,
            @RequestParam String ownerUsername,
            @RequestParam String targetUsername) {

        boolean success = postService.rejectRequest(postId, ownerUsername, targetUsername);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Action not allowed", HttpStatus.FORBIDDEN);
    }

    @PostMapping("/{postId}/remove-member")
    public ResponseEntity<?> removeMember(
            @PathVariable String postId,
            @RequestParam String targetUsername,
            Authentication auth) {

        boolean success = postService.removeMember(postId, auth.getName(), targetUsername);
        if (success) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("Action not allowed", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/delete-feed-post/{postId}")
    public ResponseEntity<?> deleteFeedPost(@PathVariable String postId, Authentication auth) {
        boolean success = postService.removePostFromFeed(postId, auth.getName());
        if (success) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>("Unauthorized or Post not found", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/delete-team/{postId}")
    public ResponseEntity<?> deleteTeam(@PathVariable String postId, Authentication auth) {
        boolean success = postService.deleteTeam(postId, auth.getName());
        if (success) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>("Unauthorized or Team not found", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/delete-post/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable String postId, Authentication auth) {
        // Backward-compatible team deletion route.
        boolean success = postService.deleteTeam(postId, auth.getName());
        if (success) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>("Unauthorized or Team not found", HttpStatus.FORBIDDEN);
    }
}