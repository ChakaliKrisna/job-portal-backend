package com.jobportal.controller;

import com.jobportal.dto.LoginRequest;
import com.jobportal.dto.LoginResponse;
import com.jobportal.dto.RegisterRequest;
import com.jobportal.entity.*;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.repository.VerificationTokenRepository;
import com.jobportal.security.JwtUtil;
import com.jobportal.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; // Added for safe delete operations
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "https://your-frontend-domain.com"
        },
        allowCredentials = "true"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepo;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        Role role;
        try {
            role = Role.valueOf("ROLE_" + request.getRole().toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid role"));
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email already exists"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setPublicId(generatePublicId(role));
        user.setEnabled(false);

        if (role == Role.ROLE_STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(user);
            user.setStudentProfile(profile);
        }

        if (role == Role.ROLE_RECRUITER) {
            RecruiterProfile profile = new RecruiterProfile();
            profile.setUser(user);
            user.setRecruiterProfile(profile);

            Company company = companyRepo.findByNameIgnoreCase(request.getCompanyName())
                    .orElseGet(() -> {
                        Company c = new Company();
                        c.setPublicId("COMP-" + UUID.randomUUID().toString().substring(0, 8));
                        c.setName(request.getCompanyName());
                        c.setDescription("Company profile not updated yet.");
                        return companyRepo.save(c);
                    });

            user.setCompany(company);
        }

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(vt);

        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173";
        }

        String link = frontendUrl + "/verify-email?token=" + token;
        emailService.sendEmail(user.getEmail(), "Verify Your Email", "Click to verify account:\n" + link);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verify email first");
        }

        String token = JwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(new LoginResponse(
                token, user.getEmail(), user.getRole().name(), user.getPublicId(), user.getName()
        ));
    }

    // ================= RESEND EMAIL =================
    @Transactional // Requires transaction state wrapper to safely drop entity structures from SQL rows
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Already verified"));
        }

        verificationTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(vt);

        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173";
        }

        String link = frontendUrl + "/verify-email?token=" + token;
        emailService.sendEmail(user.getEmail(), "Resend Verification Email", "Click to verify:\n" + link);

        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email missing");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            verificationTokenRepository.deleteByUserId(user.getId());

            VerificationToken vt = new VerificationToken();
            vt.setToken(UUID.randomUUID().toString());
            vt.setUser(user);
            vt.setExpiryDate(LocalDateTime.now().plusMinutes(30));

            verificationTokenRepository.save(vt);

            // 🔥 FIX: create reset link
            String frontendUrl = System.getenv("FRONTEND_URL");
            if (frontendUrl == null || frontendUrl.isEmpty()) {
                frontendUrl = "http://localhost:5173";
            }

            String resetLink = frontendUrl + "/reset-password?token=" + vt.getToken();

            try {
                emailService.sendEmail(
                        user.getEmail(),
                        "Reset Password",
                        "Click here: " + resetLink
                );
            } catch (Exception e) {
                System.out.println("Email failed but ignoring: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of("message", "Reset link sent"));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    // ================= RESET PASSWORD =================
    @Transactional
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {

        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required fields missing");
        }

        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (vt.getExpiryDate() == null || vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(vt);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Token expired"));
        }

        User user = vt.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        verificationTokenRepository.delete(vt);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    private String generatePublicId(Role role) {
        String prefix = switch (role) {
            case ROLE_STUDENT -> "STU";
            case ROLE_RECRUITER -> "REC";
            case ROLE_ADMIN -> "ADM";
        };

        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}