package com.example.TeamFinder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your_email@gmail.com"); // Should match your yaml username
        message.setTo(toEmail);
        message.setSubject("Your TeamFinder Verification Code");
        message.setText("Welcome to TeamFinder!\n\nYour 6-digit verification code is: " + otp +
                "\n\nThis code will expire in 5 minutes. Please do not share this code with anyone.");

        mailSender.send(message);
    }
}