package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private UserService userService;

    @GetMapping("/search-by-username/{username}")
    public ResponseEntity<?> searchUserByUsername(@PathVariable String username)
    {

        Optional<User> user = userService.searchUserByUsername(username);
        return new ResponseEntity<>(user, HttpStatusCode.valueOf(200));
    }
    @GetMapping("/search-by-name/{name}")
    public ResponseEntity<?> searchByName(@PathVariable String name)
    {
        List<User> users = userService.searchUserByName(name);
        return new ResponseEntity<>(users, HttpStatusCode.valueOf(200));
    }
    @GetMapping("/search-by-skill/{skill}")
    public ResponseEntity<?> searchBySkill(@PathVariable String skill)
    {
        List<User> users = userService.searchUserBySkill(skill);
        return new ResponseEntity<>(users, HttpStatusCode.valueOf(200));
    }
}
