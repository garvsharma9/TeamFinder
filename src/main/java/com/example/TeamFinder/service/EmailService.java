package com.example.TeamFinder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    // Pulls the variables we just added to your application.yaml
    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        // Brevo's official REST API endpoint for sending emails
        String url = "https://api.brevo.com/v3/smtp/email";
        RestTemplate restTemplate = new RestTemplate();

        // 1. Set the secure headers exactly as Brevo requires
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accept", "application/json");
        headers.set("api-key", brevoApiKey);

        // 2. Build the JSON payload using standard Java Maps
        Map<String, Object> sender = Map.of(
                "name", "TeamFinder",
                "email", senderEmail // This MUST match the email you verified on Brevo!
        );

        Map<String, Object> to = Map.of("email", toEmail);

        Map<String, Object> requestBody = Map.of(
                "sender", sender,
                "to", List.of(to),
                "subject", "Your TeamFinder Verification Code",

                // We are also upgrading your email to a nice HTML format!
                "htmlContent", "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; max-width: 500px;'>" +
                        "<h2 style='color: #0a66c2; margin-top: 0;'>TeamFinder</h2>" +
                        "<p style='font-size: 16px; color: #444;'>Your 6-digit verification code is:</p>" +
                        "<div style='background-color: #f3f2ef; padding: 15px; text-align: center; border-radius: 6px; margin: 20px 0;'>" +
                        "<span style='font-size: 32px; font-weight: bold; color: #111; letter-spacing: 6px;'>" + otp + "</span>" +
                        "</div>" +
                        "<p style='font-size: 14px; color: #888; margin-bottom: 0;'>This code will expire in 5 minutes. Please do not share this code with anyone.</p>" +
                        "</div>"
        );

        // 3. Package it up and send the HTTP POST request!
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Email successfully sent to " + toEmail + " via Brevo API!");
            } else {
                System.err.println("⚠️ Brevo returned an error: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to Brevo API: " + e.getMessage());
            e.printStackTrace(); // This will print the exact reason if it fails
        }
    }
}