package com.nhhoang.e_commerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async("taskExecutor")
    public void sendEmailAsync(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("nguyenhoanghuu15042004@gmail.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            // Log mail sending failure without blocking main business thread
            System.err.println("Failed to send async email to " + toEmail + ": " + e.getMessage());
        }
    }
}
