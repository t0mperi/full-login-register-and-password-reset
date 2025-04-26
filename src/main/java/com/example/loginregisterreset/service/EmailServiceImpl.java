package com.example.loginregisterreset.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = createPasswordResetUrl(token);
        String subject = "Password Reset Request";
        String message = "To reset your password, click the link below:\n" + resetUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);
        // Consider setting the 'from' address if needed/configured
        // email.setFrom("noreply@yourdomain.com");

        mailSender.send(email);
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject("Your Password Reset Code");
            email.setText("Your password reset code is: " + code + "\n"
                          + "This code will expire in 15 minutes.");
            mailSender.send(email);
        } catch (Exception e) {
            // Handle exception properly - log it, maybe throw a custom exception
             System.err.println("Failed to send password reset code email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset code email", e);
        }
    }

    private String createPasswordResetUrl(String token) {
        // Get the current request to build the base URL
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return baseUrl + "/reset-password?token=" + token;
    }
}
