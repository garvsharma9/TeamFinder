package com.example.TeamFinder.util; // Adjust to match your folder structure

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheScheduler {

    // Runs every 1 hour (3,600,000 milliseconds)
    // allEntries = true means it wipes the GitHub data for ALL users at once
    @CacheEvict(value = "githubRepos", allEntries = true)
    @Scheduled(fixedRateString = "3600000")
    public void clearGithubCache() {
        log.info("Hourly Maintenance: Wiping 'githubRepos' cache to ensure fresh portfolio data.");
    }
}