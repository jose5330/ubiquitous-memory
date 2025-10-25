package com.example.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.example.backend.repository.UserRepo;
import com.example.backend.model.User;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService jwtService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(8);

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return userRepo.save(user);
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
            return jwtService.generateToken(dbUser);
        } else {
            return "Authentication failed for user " + user.getUsername() + ".";
        }
    }
}
