package com.example.backend.service;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.example.backend.repository.UserRepo;
import com.example.backend.model.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.backend.model.UserPrincipal;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =  userRepo.findByUsername(username);
        if (user == null) {
            String errorString = "User: "+username+" not found";
            System.out.println(errorString);
            throw new UsernameNotFoundException(errorString);
        }
        return new UserPrincipal(user);
    }
}