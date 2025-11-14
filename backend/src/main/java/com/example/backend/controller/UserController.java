package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.backend.service.UserService;

import com.example.backend.model.User;
import com.example.backend.model.UserPrincipal;
import com.example.backend.repository.UserRepo;



@RestController()
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/verify")
    public ResponseEntity<String>  verifyToken(@RequestParam String token) {
        System.out.println("Verify attempt for token: " + token);
        return userService.verifyToken(token);
    }

    @GetMapping("/token")
    @PreAuthorize("hasRole('UNSUB')")
    public void getToken() {
        System.out.println("Get token request received");
        Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepo.findByUsername(userPrincipal.getUsername());
        System.out.println("User found: " + user.getUsername() + ", email: " + user.getEmail());
        userService.sendVerificationToken(user);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signAccount(@RequestBody User user) {
        System.out.println("Incoming user: " + (user.getId()) + " " + user.getUsername());
        return userService.signUser(user);
    }

    //@PostMapping("/register")
    public User createAccount(@RequestBody User user) {
        System.out.println("Incoming user: " + (user.getId()) + " " + user.getUsername());
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String>  login(@RequestBody User user) {
        System.out.println("Login attempt for user: " + user);
        return userService.verify(user);
    }
}
