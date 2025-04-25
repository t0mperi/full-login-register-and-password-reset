package com.example.loginregisterreset.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    private String createPasswordResetUrl(String token) {
        // Get the current request to build the base URL
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // Construct the base URL (http://localhost:8080 or your domain)
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        // Append the reset password path and token
        return baseUrl + "/reset-password?token=" + token;
    }
}
