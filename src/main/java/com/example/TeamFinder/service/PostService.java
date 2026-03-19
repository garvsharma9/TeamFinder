package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.Post;
import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.ChatMessageRepository;
import com.example.TeamFinder.repository.PostRepository;
import com.example.TeamFinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;


    public boolean savePost(Post post) {
        if (post.getVisibleInFeed() == null) {
            post.setVisibleInFeed(true);
        }
        Post savedPost = postRepository.save(post);

        Optional<User> userOpt = userRepository.findByUsername(post.getUsername());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Prevent NullPointerException
            if (user.getPosts() == null) {
                user.setPosts(new ArrayList<>());
            }
            user.getPosts().add(savedPost.getId());
            userRepository.save(user); // CRITICAL FIX: Save the user to update their post list!
            return true;
        }
        return false;
    }

    public boolean deletePost(String postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            deleteTeamResources(post);
            return true;
        }
        return false;
    }

    // Note: To prevent infinite liking/disliking, you should eventually
    // track WHO liked the post, just like we did with User profiles!
    public boolean like(String postId) {
        Optional<Post> byId = postRepository.findById(postId);
        if (byId.isPresent()) {
            Post post = byId.get();
            post.setLike(post.getLike() + 1);
            postRepository.save(post);
            return true;
        }
        return false;
    }

    public boolean dislike(String postId) {
        Optional<Post> byId = postRepository.findById(postId);
        if (byId.isPresent()) {
            Post post = byId.get();
            post.setDislike(post.getDislike() + 1);
            postRepository.save(post);
            return true;
        }
        return false;
    }

    // ... (Keep your existing savePost, deletePost, like, and dislike methods) ...

    // 1. Fetch all posts so we can display them on a "Feed" page
//    public List<Post> getAllPosts() {
//        return postRepository.findAll();
//    }

    // 2. A user sends a request to join a team
    public boolean requestToJoin(String postId, String requesterUsername) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            // Initialize lists if they are null (safety check)
            if (post.getRequestedUsernames() == null) post.setRequestedUsernames(new ArrayList<>());
            if (post.getAcceptedUsernames() == null) post.setAcceptedUsernames(new ArrayList<>());

            // Prevent the owner from requesting to join their own team
            if (post.getUsername().equals(requesterUsername)) {
                return false;
            }

            // Prevent duplicate requests or requesting if already accepted
            if (post.getRequestedUsernames().contains(requesterUsername) ||
                    post.getAcceptedUsernames().contains(requesterUsername)) {
                return false;
            }

            post.getRequestedUsernames().add(requesterUsername);
            postRepository.save(post);
            return true;
        }
        return false;
    }

    // 3. The owner accepts a join request
    public boolean acceptRequest(String postId, String ownerUsername, String targetUsername) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            // Authorization Check: Only the post creator can accept members
            if (!post.getUsername().equals(ownerUsername)) {
                return false;
            }

            // Move the user from 'requested' to 'accepted'
            if (post.getRequestedUsernames() != null && post.getRequestedUsernames().contains(targetUsername)) {
                post.getRequestedUsernames().remove(targetUsername);

                if (post.getAcceptedUsernames() == null) post.setAcceptedUsernames(new ArrayList<>());
                post.getAcceptedUsernames().add(targetUsername);

                postRepository.save(post);
                return true;
            }
        }
        return false;
    }

    // 4. The owner rejects a join request
    public boolean rejectRequest(String postId, String ownerUsername, String targetUsername) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            // Authorization Check
            if (!post.getUsername().equals(ownerUsername)) {
                return false;
            }

            // Remove the user from the requested list
            if (post.getRequestedUsernames() != null) {
                post.getRequestedUsernames().remove(targetUsername);
                postRepository.save(post);
                return true;
            }
        }
        return false;
    }

    public boolean removeMember(String postId, String ownerUsername, String targetUsername) {
        Optional<Post> postOpt = postRepository.findById(postId);

        if (postOpt.isPresent()) {
            Post post = postOpt.get();

            if (!matchesUser(post.getUsername(), ownerUsername) || matchesUser(post.getUsername(), targetUsername)) {
                return false;
            }

            if (post.getAcceptedUsernames() == null) {
                return false;
            }

            boolean removed = post.getAcceptedUsernames().removeIf(member -> matchesUser(member, targetUsername));
            if (!removed) {
                return false;
            }

            postRepository.save(post);
            return true;
        }
        return false;
    }

    public List<Post> getFeedPosts() {
        List<Post> posts = postRepository.findAll().stream()
                .filter(this::isVisibleInFeed)
                .toList();

        attachProfilePictures(posts);
        return posts;
    }


    // 1. Updated get all posts (Attaches the photo!)
    public List<Post> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        attachProfilePictures(posts);
        return posts;
    }

    public boolean removePostFromFeed(String postId, String requesterUsername) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            if (matchesUser(post.getUsername(), requesterUsername)) {
                post.setVisibleInFeed(false);
                postRepository.save(post);
                return true;
            }
        }
        return false;
    }

    public boolean deleteTeam(String postId, String requesterUsername) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            if (matchesUser(post.getUsername(), requesterUsername)) {
                deleteTeamResources(post);
                return true;
            }
        }
        return false;
    }

    private void attachProfilePictures(List<Post> posts) {
        for (Post post : posts) {
            userRepository.findByUsername(post.getUsername()).ifPresent(user -> {
                post.setProfilePictureUrl(user.getProfilePictureUrl());
            });
        }
    }

    private boolean isVisibleInFeed(Post post) {
        return post.getVisibleInFeed() == null || Boolean.TRUE.equals(post.getVisibleInFeed());
    }

    private void deleteTeamResources(Post post) {
        removePostIdFromOwner(post.getUsername(), post.getId());
        chatMessageRepository.deleteByTeamId(post.getId());
        postRepository.deleteById(post.getId());
    }

    private void removePostIdFromOwner(String username, String postId) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        if (user.getPosts() == null) {
            return;
        }

        user.getPosts().removeIf(existingPostId -> normalize(existingPostId).equals(normalize(postId)));
        userRepository.save(user);
    }

    private boolean matchesUser(String first, String second) {
        return normalize(first).equals(normalize(second));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}