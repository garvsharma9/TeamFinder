package com.example.TeamFinder.repository;

import com.example.TeamFinder.entity.Event;
import com.example.TeamFinder.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {
    @Query("{}")
    List<Event> getEvents();
    List<Event> findByHeadingIn(List<Pattern> patterns);
// (Again, change 'EventName' to whatever your Event title variable is named!)
}
