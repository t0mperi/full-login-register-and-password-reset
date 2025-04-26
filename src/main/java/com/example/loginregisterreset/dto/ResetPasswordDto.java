package com.example.loginregisterreset.dto;

import jakarta.validation.constraints.NotEmpty;

public class ResetPasswordDto {

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

    // Standard getters and setters

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
