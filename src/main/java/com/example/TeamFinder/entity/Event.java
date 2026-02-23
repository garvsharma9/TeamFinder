package com.example.TeamFinder.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("events")
@Data
public class Event {
    @Id
    private String id;
    private String clubName;
    private String title;
    private String description;
}
