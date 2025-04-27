package com.example.loginregisterreset.service;

public interface EmailService {
    void sendPasswordResetEmail(String to, String token);
    void sendPasswordResetCode(String to, String code); 
}
