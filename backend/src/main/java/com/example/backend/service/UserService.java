package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.example.backend.repository.UserRepo;
import com.example.backend.model.User;
import com.example.backend.model.VerificationToken;
import com.example.backend.repository.VerificationTokenRepo;
import com.example.backend.service.JWTService;
import com.example.backend.service.MyUserDetailsService;

@Service
public class UserService {
    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepo tokenRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    ResponseEntity<String> Cookie(String token) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")    
            .path("/")
            .domain("connecthub-g1qb.onrender.com") 
            .maxAge(10 * 60 * 60)  // 10 hours
            .build();
        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body("Logged in");
    }

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);

    public ResponseEntity<String> verifyToken(String token) {
        VerificationToken verification = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = verification.getUser();
        user.setRole("ROLE_USER");
        user = userRepo.save(user);

        tokenRepo.delete(verification);
        return Cookie(jwtService.generateToken(user));
    }

    public User registerUser(User user) {
        user.setRole("ROLE_USER");
        return userRepo.save(user);
    }

    public ResponseEntity<String> signUser(User user) {
        User nonDB = user;

        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_UNSUB");

        user = userRepo.save(user);
        
        return Cookie(jwtService.generateToken(user));
    }

    public void sendVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepo.save(verificationToken);
        
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public ResponseEntity<String> verify(User user) {
        User dbUser = userRepo.findByUsername(user.getUsername());
        if (dbUser == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        System.out.println("Class of user: " + user.getClass());
        System.out.println("Role value: " + user.getRole());

        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()) );
        if (authentication.isAuthenticated()) {
            System.out.println("yo uuh this should work");
            return Cookie(jwtService.generateToken(user));
        } else {
            return ResponseEntity.status(401).body("Authentication failed");
        }
    }
}
