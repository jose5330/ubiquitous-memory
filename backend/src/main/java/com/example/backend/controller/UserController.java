package com.example.backend.controller;

import java.util.Map;

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
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.service.UserService;

import jakarta.mail.Multipart;
import jakarta.servlet.http.HttpServletRequest;

import com.example.backend.model.User;
import com.example.backend.model.UserPrincipal;
import com.example.backend.repository.UserRepo;

import com.example.backend.service.S3Service;



@RestController()
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private S3Service s3Service;

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
        return ResponseEntity.ok(Map.of("authenticated", false));
    }

    UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
    User user = userRepo.findByUsername(userPrincipal.getUsername());
    return ResponseEntity.ok(Map.of(
        "authenticated", true,
        "username", user.getUsername(),
        "role", user.getRole(),
        "id", user.getId(),
        "userAvatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
    ));
}

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
    
    @PostMapping("/uploadAvatar")
    @PreAuthorize("hasRole('USER')")
    public void uploadAvatar(@RequestParam("file") MultipartFile fileData) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepo.findByUsername(userPrincipal.getUsername());
        String fileUrl = s3Service.uploadFile(fileData, user.getUsername());
        System.out.println("File uploaded to URL: " + fileUrl );
        user.setAvatarUrl(fileUrl);
        userRepo.save(user);
        System.out.println("Updating user avatar URL to: " + user.getAvatarUrl());
    } 
}
