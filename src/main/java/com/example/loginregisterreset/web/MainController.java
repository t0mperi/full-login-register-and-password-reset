package com.example.loginregisterreset.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Return the view name (login.html)
    }

    @GetMapping("/index")
    public String home() {
        return "index"; // Return the view name (index.html)
    }

    // Optional: Redirect root path to index if logged in, or login if not.
    // Spring Security handles redirection to login if not authenticated for protected paths.
    // If you want a specific landing page logic, you can add it here.
    /*
    @GetMapping("/")
    public String root() {
        // Check authentication status if needed, otherwise redirect
        return "redirect:/index"; // Or redirect:/login if you prefer
    }
    */
}
