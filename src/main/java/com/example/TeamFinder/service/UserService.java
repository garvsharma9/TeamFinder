package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private static final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
    public boolean signup(User user)
    {
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(user.getPassword())));
        User insert = userRepository.insert(user);
        return true;

    }
}
