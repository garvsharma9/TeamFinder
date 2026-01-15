package com.example.TeamFinder.controller;

import com.example.TeamFinder.service.PublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private PublicService publicService;

    @GetMapping("/health-check")
    public ResponseEntity<?> heathCheck()
    {
        return new ResponseEntity<>(publicService.healthCheck(), HttpStatusCode.valueOf(200));
    }

}
