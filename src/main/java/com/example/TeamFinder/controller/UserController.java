package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody User user)
    {
        userService.signup(user);
        return new ResponseEntity<>(HttpStatus.valueOf(200));
    }

}
