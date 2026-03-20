package com.example.TeamFinder.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PublicService {
    public String healthCheck()
    {
        return "OK";
    }

    @Cacheable(value = "githubRepos", key = "#username")
    public List<?> getGithubRepos(String username) {
        RestTemplate restTemplate = new RestTemplate();
        // Fetch 30 repos, exactly like we did in React
        String url = "https://api.github.com/users/" + username + "/repos?sort=updated&per_page=30";

        try {
            // Make the call to GitHub
            return restTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            // If GitHub fails (e.g., user doesn't exist), return null so we don't cache an error
            return null;
        }
    }
}
