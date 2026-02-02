package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.Post;
import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.PostRepository;
import com.example.TeamFinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean savePost(Post post)
    {
        Post save = postRepository.save(post);

        Optional<User> byUsername = userRepository.findByUsername(save.getUsername());
        byUsername.get().getPosts().add(save.getId());
        return true;
    }
    public boolean deletePost(Post post)
    {
        postRepository.delete(post);
        return true;
    }
    public boolean like(String postId)
    {
        Optional<Post> byId = postRepository.findById(postId);
        byId.get().setLike(byId.get().getLike()+1);
        postRepository.save(byId.get());
        return true;
    }
    public boolean dislike(String postId)
    {
        Optional<Post> byId = postRepository.findById(postId);
        byId.get().setDislike(byId.get().getDislike()+1);
        postRepository.save(byId.get());
        return true;
    }
}
