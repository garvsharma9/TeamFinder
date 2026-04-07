package com.example.TeamFinder.service;

import com.example.TeamFinder.entity.Event;
import com.example.TeamFinder.entity.Post;
import com.example.TeamFinder.entity.User;
import com.example.TeamFinder.repository.EventRepository;
import com.example.TeamFinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Async
    public void notifyUsersAboutNewPost(Post newPost) {
        String compName = newPost.getCompetitionName();
        if (compName == null || compName.trim().isEmpty()) {
            return;
        }

        // 1. Generate search terms for exact-word, case-insensitive matching
        List<String> searchTerms = new ArrayList<>();
        searchTerms.add(compName.trim());

        String[] words = compName.split("\\s+");
        for (String word : words) {
            if (word.length() > 2) { // Ignore small connecting words
                searchTerms.add(word);
            }
        }

        List<Pattern> patterns = searchTerms.stream()
                .map(term -> Pattern.compile("^" + Pattern.quote(term) + "$", Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());

        // 2. Fetch the matching Events from the database
        // NOTE: If your Event entity uses a different field name for the title (like "title" or "name"),
        // you'll need to change this method call to match your repository (e.g., findByTitleIn)
        List<Event> matchedEvents = eventRepository.findByHeadingIn(patterns);

        // 3. Extract unique usernames of people who liked these events
        Set<String> interestedUsernames = new HashSet<>();
        for (Event event : matchedEvents) {
            if (event.getLike() != null) {
                interestedUsernames.addAll(event.getLike());
            }
        }

        // Remove the person who just created the new post so they don't email themselves
        interestedUsernames.remove(newPost.getUsername());

        // If no one has liked the event, stop here
        if (interestedUsernames.isEmpty()) {
            return;
        }

        // 4. Fetch the actual User objects to get their emails using your existing method
        List<User> usersToNotify = userRepository.findByUsernameIn(new ArrayList<>(interestedUsernames));

        // 5. Send the recommendation emails
        for (User user : usersToNotify) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String receiverName = user.getName() != null ? user.getName() : user.getUsername();

                emailService.sendTeamRecommendationEmail(
                        user.getEmail(),
                        receiverName,
                        newPost.getCompetitionName(),
                        newPost.getPosition(),
                        newPost.getId()
                );
            }
        }
    }
}