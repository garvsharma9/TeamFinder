package com.example.TeamFinder.controller;

import com.example.TeamFinder.entity.Event;
import com.example.TeamFinder.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
@Controller
@RequestMapping("/event")
@Slf4j
public class EventController {

    @Autowired
    private EventService eventService;

    // GET /api/events
    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        // Fetch all official events to display by default on the Events page

        List<Event> events = eventService.eventsList();
        return new ResponseEntity<>(events, HttpStatusCode.valueOf(200));
    }
}
