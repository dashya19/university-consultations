package com.university.consultations.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    @GetMapping("/redirectByRole")
    public String redirectByRole(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_Студент")) {
                    return "redirect:/student/dashboard";
                } else if (authority.getAuthority().equals("ROLE_Преподаватель")) {
                    return "redirect:/teacher/dashboard";
                } else if (authority.getAuthority().equals("ROLE_Руководство")) {
                    return "redirect:/management/dashboard";
                }
            }
        }
        return "redirect:/login?error=unknown_role";
    }
}
