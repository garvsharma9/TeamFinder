package com.example.TeamFinder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PublicService {

    // Pulls the token from your application.properties
    @Value("${github.api.token}")
    private String githubToken;

    public String healthCheck() {
        return "OK";
    }

    @Cacheable(value = "githubRepos", key = "#username")
    public List<?> getGithubRepos(String username) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.github.com/users/" + username + "/repos?sort=updated&per_page=30";

        try {
            // 1. Create the Headers and add the token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + githubToken);

            // 2. Wrap the headers in an HttpEntity
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 3. Use exchange() instead of getForObject() to pass the entity
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            return response.getBody();

        } catch (Exception e) {
            // If GitHub fails, return null to avoid caching the error
            return null;
        }
    }
}