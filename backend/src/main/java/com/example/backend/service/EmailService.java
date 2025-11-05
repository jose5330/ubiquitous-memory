package com.example.backend.service;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        String link = "https://your-frontend.com/verify?token=" + token;
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject("Verify your email");
        email.setText("Click the link to verify: " + link);
        mailSender.send(email);
    }
}
