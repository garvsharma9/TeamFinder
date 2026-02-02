package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.Post;
import com.example.TeamFinder.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/post")
@RestController
@Slf4j
public class PostEntryController {

    @Autowired
    private PostService postService;

    @PostMapping("/add-post")
    public ResponseEntity<?> createPost(@RequestBody Post post)
    {
        try {
            boolean b = postService.savePost(post);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));

        }
        catch (Exception e)
        {
            log.error("Error while creating post ");
            return new ResponseEntity<>(HttpStatusCode.valueOf(400));
        }
    }
    @DeleteMapping("/delete-post")
    public ResponseEntity<?> deletePost(@RequestBody Post post)
    {
        postService.deletePost(post);
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }

    @PutMapping("/like")
    public ResponseEntity<?> like(@RequestBody String postid)
    {
        postService.like(postid);
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));

    }
    @PutMapping("/dislike")
    public ResponseEntity<?> dislike(@RequestBody String postid)
    {
        postService.dislike(postid);
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));

    }
}
