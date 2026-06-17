package com.jobportal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("JWT FILTER -> " + method + " " + path);

        // ✅ 1. Skip CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 2. Public endpoints (NO JWT REQUIRED)
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 3. Get Authorization header
        String header = request.getHeader("Authorization");

        // 👉 If no token → continue (Spring Security will decide)
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            // ✅ 4. Validate token
            if (JwtUtil.validateToken(token)) {

                String email = JwtUtil.extractEmail(token);
                String role = JwtUtil.extractRole(token);

                if (role == null) {
                    role = "ROLE_USER";
                }

                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            System.out.println("JWT ERROR: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
    private boolean isPublicEndpoint(String path) {

        return path.startsWith("/auth/") ||
                path.startsWith("/job-portal/jobs") ||   // 👈 YOUR MAIN FIX
                path.startsWith("/uploads/") ||
                path.contains("/swagger") ||
                path.contains("/api-docs");
    }
}