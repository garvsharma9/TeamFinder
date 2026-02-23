package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.Event;
import com.example.TeamFinder.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> eventsList()
    {
        List<Event> events = eventRepository.getEvents();
        return events;
    }

}
