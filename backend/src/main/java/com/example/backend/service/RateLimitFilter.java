package com.example.backend.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.backend.model.UserPrincipal;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        // Example: 5 posts per minute
        Bandwidth limit = Bandwidth.builder()
            .capacity(3)
            .refillGreedy(3, Duration.ofMinutes(1))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = authentication != null ? (UserPrincipal) authentication.getPrincipal() : null;

        // Apply only to POST requests under /api/posts
        System.out.println("RateLimitFilter is running for " + request.getMethod());
        if (request.getRequestURI().startsWith("/api/user") && request.getMethod().equals("POST")) {
            
            String userIdentifier = userPrincipal != null ? userPrincipal.getUsername() : request.getRemoteAddr();
            
            System.out.println("RateLimitFilter:  "+userIdentifier + "  " + userPrincipal);
            Bucket bucket = buckets.computeIfAbsent(userIdentifier, k -> createBucket());

            if (bucket.tryConsume(1)) {
                System.out.println("RateLimitFilter: allowed: " + bucket.getAvailableTokens()+" tokens left");
                chain.doFilter(request, response);
            } else {
                System.out.println("RateLimitFilter : blocked");
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("Rate limit exceeded. Try again later.");
                return;
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
