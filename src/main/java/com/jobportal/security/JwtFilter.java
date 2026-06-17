package com.jobportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authorization: " + request.getHeader("Authorization"));

        String path = request.getRequestURI();

        // Skip auth APIs
        if (path.startsWith("/auth/")
                || path.startsWith("/uploads/")) {

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            try {

                String token = authHeader.substring(7);

                if (JwtUtil.validateToken(token)) {

                    String email = JwtUtil.extractEmail(token);
                    String role = JwtUtil.extractRole(token);

                    if (role != null) {

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        email,
                                        null,
                                        List.of(new SimpleGrantedAuthority(role))
                                );

                        SecurityContextHolder
                                .getContext()
                                .setAuthentication(authentication);

                        System.out.println("Authenticated: " + email);
                        System.out.println("Role: " + role);
                    }
                }

            } catch (Exception e) {

                System.out.println("JWT Error: " + e.getMessage());

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);


    }
}