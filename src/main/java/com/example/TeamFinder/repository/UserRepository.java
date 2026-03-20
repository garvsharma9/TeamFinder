package com.example.TeamFinder.repository;

import com.example.TeamFinder.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String s);
    List<User> findByName(String name);
    List<User> findBySkill(String skill);
    Optional<User> findFirstByEmail(String email);
    // Spring Data JPA will automatically write the SQL query for this!
    List<User> findByUsernameIn(List<String> usernames);
    // Assuming 'skill' is an @ElementCollection in your User entity
    List<User> findDistinctBySkillInAndUsernameNot(List<String> skills, String username);

    // Fallback query if they have no skills yet
    List<User> findTop10ByUsernameNot(String username);
}
