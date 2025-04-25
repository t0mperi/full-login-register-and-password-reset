package com.example.loginregisterreset.web;

import com.example.loginregisterreset.dto.ForgotPasswordDto;
import com.example.loginregisterreset.dto.ResetPasswordDto;
import com.example.loginregisterreset.entity.PasswordResetToken;
import com.example.loginregisterreset.entity.User;
import com.example.loginregisterreset.repository.PasswordResetTokenRepository;
import com.example.loginregisterreset.repository.UserRepository;
import com.example.loginregisterreset.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    @ModelAttribute("forgotPasswordDto")
    public ForgotPasswordDto forgotPasswordDto() {
        return new ForgotPasswordDto();
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@ModelAttribute("forgotPasswordDto") @Valid ForgotPasswordDto forgotPasswordDto,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "forgot-password";
        }

        User user = userRepository.findByEmail(forgotPasswordDto.getEmail());
        if (user == null) {
            // Optionally: Show a generic message even if user not found for security
            redirectAttributes.addFlashAttribute("successMessage", "If an account exists for this email, a password reset link has been sent.");
            return "redirect:/forgot-password?success";
        }

        // Create and save token
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(tokenValue, user);
        tokenRepository.save(token);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);

        redirectAttributes.addFlashAttribute("successMessage", "A password reset link has been sent to your email.");
        return "redirect:/forgot-password?success";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(required = false) String token, Model model, RedirectAttributes redirectAttributes) {
        if (token == null || token.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid password reset token.");
            return "redirect:/login"; // Or show an error page
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired password reset token.");
            return "redirect:/login"; // Or show an error page
        }

        // Add token to model for the form
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setToken(token);
        model.addAttribute("resetPasswordDto", resetPasswordDto);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    @Transactional // Ensure atomicity: update password and delete token together
    public String handleResetPassword(@ModelAttribute("resetPasswordDto") @Valid ResetPasswordDto resetPasswordDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {

        if (!resetPasswordDto.getPassword().equals(resetPasswordDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Passwords do not match");
        }

        // Re-validate token just in case
        PasswordResetToken token = tokenRepository.findByToken(resetPasswordDto.getToken());
        if (token == null || token.isExpired()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired password reset token.");
            return "redirect:/login"; // Redirect back to login
        }

        if (result.hasErrors()) {
            // Need to add token back to the model if returning to the form
            // Or handle this differently, e.g., redirect with error
            return "reset-password"; // Stay on the reset form
        }

        // Update password
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
        userRepository.save(user);

        // Delete the used token
        tokenRepository.delete(token);

        redirectAttributes.addFlashAttribute("successMessage", "Your password has been successfully reset. Please login.");
        return "redirect:/login?resetSuccess";
    }
}
