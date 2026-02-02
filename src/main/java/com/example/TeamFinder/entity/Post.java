package com.example.TeamFinder.entity;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
@Document("post")
public class Post {
    @Id
    private String id;
    private String username;
    private Integer like;
    private Integer dislike;
    private String body;
    private String heading;
}
