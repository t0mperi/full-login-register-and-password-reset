package com.example.loginregisterreset.web;
import org.springframework.security.core.Authentication;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    @GetMapping("/login")
    public String login() {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/index";
        }
        return "login"; 
    }
    
   
    @GetMapping("/index")
    public String home() {
        return "index"; 
    }
  
    @GetMapping("/")
    public String root() {
        return "redirect:/index";  
    }
   
}
