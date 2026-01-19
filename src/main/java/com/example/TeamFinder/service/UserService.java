package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public ResponseEntity<?> signin(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            if(passwordEncoder.matches(user.getPassword(), byUsername.get().getPassword()))
            {
                return new ResponseEntity<>(byUsername.get(), HttpStatusCode.valueOf(200));
            }
            else
            {
                return new ResponseEntity<>(HttpStatusCode.valueOf(401));
            }
        }
        else return new ResponseEntity<>(HttpStatusCode.valueOf(404));
    }
    public boolean changeBio(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            byUsername.get().setBio(user.getBio());
            userRepository.insert(byUsername.get());
            return true;
        }
        else
            return false;
    }
    public boolean addEmail(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            byUsername.get().setEmail(user.getEmail());
            userRepository.insert(byUsername.get());
            return true;
        }
        else
            return false;
    }
    public boolean deleteEmail(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            byUsername.get().setEmail("");
            userRepository.insert(byUsername.get());
            return true;
        }
        else
            return false;
    }
    public boolean addCollege(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            byUsername.get().setCollege(user.getCollege());
            userRepository.insert(byUsername.get());
            return true;
        }
        else
            return false;
    }
    public boolean deleteCollege(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            byUsername.get().setCollege("");
            userRepository.insert(byUsername.get());
            return true;
        }
        else
            return false;
    }
    public List<User> searchUserByName(String name)
    {
        List<User> byName = userRepository.findByName(name);
        return byName;
    }
    public Optional<User> searchUserByUsername(String username)
    {
        return userRepository.findByUsername(username);
    }
    public  List<User> searchUserBySkill(String skill)
    {
        return userRepository.findBySkill(skill);
    }
    public boolean addSkill(User user)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            for(String skill:user.getSkill())
            {
                byUsername.get().getSkill().add(skill);
            }
            userRepository.insert(byUsername.get());
            return true;
        }
        else
        {
            return false;
        }
    }
    public boolean removeSkill(User user, String skill)
    {
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(!byUsername.isEmpty())
        {
            byUsername.get().getSkill().remove(skill);
            userRepository.insert(byUsername.get());
            return true;
        }
        else
        {
            return false;
        }
    }

}
