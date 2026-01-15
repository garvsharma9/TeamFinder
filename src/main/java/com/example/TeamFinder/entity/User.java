package com.example.TeamFinder.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("user")
@Data
public class User {
    @Id
    private ObjectId id;
    @NonNull
    private String username;
    @NonNull
    private String password;
    private String email;
    private String college;

}
