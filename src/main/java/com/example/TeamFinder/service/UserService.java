package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private static final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
    public boolean signup(User user)
    {
        user.setPassword(Objects.requireNonNull(passwordEncoder.encode(user.getPassword())));
        user.setRoles(Arrays.asList("USER"));
        User insert = userRepository.insert(user);
        return true;

    }
    public void saveAdmin(User user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(Arrays.asList("USER", "ADMIN"));
            userRepository.save(user);
        }catch(Exception e)
        {
            log.error("Error", e);
        }
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
            userRepository.save(byUsername.get());
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
            userRepository.save(byUsername.get());
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
            userRepository.save(byUsername.get());
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
            userRepository.save(byUsername.get());
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
            userRepository.save(byUsername.get());
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
            userRepository.save(byUsername.get());
            return true;
        }
        else
        {
            return false;
        }
    }

}
