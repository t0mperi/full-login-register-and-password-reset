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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Random;

@Controller
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
     

        // Check if user exists
        User user = userRepository.findByEmail(forgotPasswordDto.getEmail());
        if (user == null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "If an account exists for this email, a password reset code has been sent.");
            return "redirect:/forgot-password?success";
        }

        // Generate 6-digit code
        Random random = new Random();
        String code = String.format("%06d", random.nextInt(999999));

        // Find existing token for user and delete if present
        PasswordResetToken existingToken = tokenRepository.findByUserId(user.getId());
        if (existingToken != null) {
            tokenRepository.delete(existingToken);
        }

        // Create and save new token with the code
        PasswordResetToken token = new PasswordResetToken(code, user);
        tokenRepository.save(token);

        // Send email with the code
        try {
            emailService.sendPasswordResetCode(user.getEmail(), code);
        } catch (Exception e) {
            // Log error appropriately
            System.err.println("Failed to send password reset code email: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not send reset code email. Please try again later.");
            return "forgot-password";
        }

        redirectAttributes.addFlashAttribute("successMessage",
                "A 6-digit password reset code has been sent to your email.");
        // Redirect to the new page, passing email
        return "redirect:/enter-reset-code?email=" + user.getEmail();
    }

    // Show the reset password form
    @GetMapping("/enter-reset-code")
    public String showEnterCodeForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        // Add success message if redirected from forgot-password
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        return "enter-reset-code";
    }
    // Validate the reset code
    @PostMapping("/validate-reset-code")
    public String handleValidateCode(@RequestParam("email") String email,
            @RequestParam("code") String code,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid request.");
            return "redirect:/forgot-password";
        }
        // Find the token for the user
        PasswordResetToken token = tokenRepository.findByUserId(user.getId());
        if (token == null || !token.getToken().equals(code) || token.isExpired()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid or expired reset code.");
            // Pass email back to the form
            redirectAttributes.addAttribute("email", email);
            return "redirect:/enter-reset-code";
        }

        // Code is valid, store confirmation in session
        HttpSession session = request.getSession();
        session.setAttribute("reset_password_email", email);
        session.setAttribute("reset_code_validated", true);

        // Redirect to the actual password reset page
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        Boolean validated = (Boolean) session.getAttribute("reset_code_validated");
        String email = (String) session.getAttribute("reset_password_email");

        if (validated == null || !validated || email == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid session. Please start the password reset process again.");
            return "redirect:/forgot-password";
        }

        // If validated, show the form with an empty DTO
        model.addAttribute("resetPasswordDto", new ResetPasswordDto());
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    @Transactional
    public String handleResetPassword(@ModelAttribute("resetPasswordDto") @Valid ResetPasswordDto resetPasswordDto,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Boolean validated = (Boolean) session.getAttribute("reset_code_validated");
        String email = (String) session.getAttribute("reset_password_email");

        // Double-check session validity
        if (validated == null || !validated || email == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Invalid session or session expired. Please start the password reset process again.");
            return "redirect:/forgot-password";
        }

        // Check if passwords match
        if (!resetPasswordDto.getPassword().equals(resetPasswordDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Passwords do not match");
        }

        // Check if password is strong
        if (!resetPasswordDto.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")) {
            result.rejectValue("password", null, "Password must be at least 8 characters long and contain at least one digit, one uppercase letter, one lowercase letter, and one special character");
        }

        // If form has validation errors (like @NotEmpty or mismatch)
        if (result.hasErrors()) {
            return "reset-password";
        }

        // Validation passed, update password
        User user = userRepository.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found. Invalid session data.");
            session.invalidate();
            return "redirect:/forgot-password";
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDto.getPassword()));
        userRepository.save(user);

        // Delete the used token
        PasswordResetToken token = tokenRepository.findByUserId(user.getId());
        if (token != null) {
            tokenRepository.delete(token);
        }

        // Invalidate session attributes to prevent reuse
        session.removeAttribute("reset_code_validated");
        session.removeAttribute("reset_password_email");

        redirectAttributes.addFlashAttribute("successMessage",
                "Your password has been successfully reset. Please login.");
        return "redirect:/login?resetSuccess";
    }
}
