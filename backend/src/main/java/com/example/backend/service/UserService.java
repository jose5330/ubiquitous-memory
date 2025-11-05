package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);

    public String verifyToken(String token) {
        VerificationToken verification = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = verification.getUser();
        user.setRole("ROLE_USER");
        user = userRepo.save(user);

        tokenRepo.delete(verification);
        return jwtService.generateToken(user);
    }

    public User registerUser(User user) {
        user.setRole("ROLE_USER");
        return userRepo.save(user);
    }

    public String signUser(User user) {
        User nonDB = user;

        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_UNSUB");

        user = userRepo.save(user);
        
        String jwt = jwtService.generateToken(user);
        return jwt;
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

    public String verify(User user) {
        User dbUser = userRepo.findByUsername(user.getUsername());
        if (dbUser == null) {
            return "User " + user.getUsername() + " not found.";
        }

        System.out.println("Class of user: " + user.getClass());
        System.out.println("Role value: " + user.getRole());

        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()) );
        if (authentication.isAuthenticated()) {
            System.out.println("yo uuh this should work");
            return jwtService.generateToken(dbUser);
        } else {
            return "Authentication failed for user " + user.getUsername() + ".";
        }
    }
}
