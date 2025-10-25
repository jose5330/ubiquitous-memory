package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.backend.service.UserService;

import com.example.backend.model.User;

@RestController()
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public User createAccount(@RequestBody User user) {
        System.out.println("Incoming user: " + (user.getId()) + " " + user.getUsername());
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        System.out.println("Login attempt for user: " + user);
        return userService.verify(user);
    }
}
