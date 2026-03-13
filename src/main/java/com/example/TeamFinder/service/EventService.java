package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.Event;
import com.example.TeamFinder.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public boolean saveEvent(Event event) {
        try {
            eventRepository.save(event);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteEvent(String id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void likeEvent(String eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        // Safety check to prevent NullPointerExceptions
        if (event.getLike() == null) event.setLike(new ArrayList<>());
        if (event.getDislike() == null) event.setDislike(new ArrayList<>());

        if (event.getLike().contains(username)) {
            // If they already liked it, clicking again removes their like (Toggle off)
            event.getLike().remove(username);
        } else {
            // Add to likes, and guarantee they are removed from dislikes
            event.getLike().add(username);
            event.getDislike().remove(username);
        }

        eventRepository.save(event);
    }

    public void dislikeEvent(String eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getLike() == null) event.setLike(new ArrayList<>());
        if (event.getDislike() == null) event.setDislike(new ArrayList<>());

        if (event.getDislike().contains(username)) {
            // Toggle off dislike
            event.getDislike().remove(username);
        } else {
            // Add to dislikes, and guarantee they are removed from likes
            event.getDislike().add(username);
            event.getLike().remove(username);
        }

        eventRepository.save(event);
    }
}