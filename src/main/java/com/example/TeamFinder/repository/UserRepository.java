package com.example.TeamFinder.repository;

import com.example.TeamFinder.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Keep this for Authentication/Login (exact match)
    Optional<User> findByUsername(String s);
    Optional<User> findFirstByEmail(String email);

    // --- NEW: Fuzzy Search Methods ---
    List<User> findByUsernameContainingIgnoreCase(String username);
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findBySkillContainingIgnoreCase(String skill);
    // ---------------------------------

    List<User> findByUsernameIn(List<String> usernames);
    List<User> findDistinctBySkillInAndUsernameNot(List<String> skills, String username);
    List<User> findTop10ByUsernameNot(String username);
}