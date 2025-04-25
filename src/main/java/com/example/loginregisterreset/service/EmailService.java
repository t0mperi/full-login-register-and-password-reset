package com.example.loginregisterreset.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
    // Add other email sending methods if needed later
}
