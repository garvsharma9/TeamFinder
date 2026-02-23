package com.example.TeamFinder.repository;

import com.example.TeamFinder.entity.Event;
import com.example.TeamFinder.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> getEvents();
}
