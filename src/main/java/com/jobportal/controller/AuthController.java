package com.jobportal.controller;

import com.jobportal.dto.LoginRequest;
import com.jobportal.dto.RegisterRequest;
import com.jobportal.entity.*;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.repository.VerificationTokenRepository;
import com.jobportal.security.JwtUtil;
import com.jobportal.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.jobportal.dto.LoginResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@CrossOrigin(origins = "http://localhost:5173") // or "*" for testing
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
//@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final CompanyRepository companyRepo;
    @Autowired
    private final VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private final EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // ================= PROFILE CREATION =================
        if (role == Role.ROLE_STUDENT) {
            StudentProfile profile = new StudentProfile();
            profile.setUser(user);
            user.setStudentProfile(profile);

        }

        if (role == Role.ROLE_RECRUITER) {

            RecruiterProfile profile = new RecruiterProfile();
            profile.setUser(user);
            user.setRecruiterProfile(profile);

            Optional<Company> existingCompany =
                    companyRepo.findByNameIgnoreCase(
                            request.getCompanyName()
                    );

            Company company;

            if(existingCompany.isPresent()) {

                company = existingCompany.get();

            } else {

                company = new Company();

                company.setPublicId(
                        "COMP-" + UUID.randomUUID()
                                .toString()
                                .substring(0,8)
                );

                company.setName(request.getCompanyName());
//                company.setLogoUrl("/images/default-company.png");
                company.setDescription("Company profile not updated yet.");
                company = companyRepo.save(company);
            }

            user.setCompany(company);
        }

        User savedUser = userRepository.save(user);

        // ================= TOKEN GENERATION =================
        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(savedUser);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));

        verificationTokenRepository.save(vt);

        // ================= EMAIL LINK =================
        String link =
                "http://localhost:8080/job-portal/auth/verify-email?token=" + token;

        emailService.sendEmail(
                savedUser.getEmail(),
                "Verify Your Email",
                "Click here to verify:\n" + link
        );

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        System.out.println("trying to login");

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        User existing = userOpt.get();

        // ================= PASSWORD CHECK =================
        if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid password"));
        }

        // ================= EMAIL VERIFICATION CHECK (IMPORTANT) =================
        if (!existing.isEnabled()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please verify your email before login"));
        }

        // ================= JWT GENERATION =================
        String token = JwtUtil.generateToken(
                existing.getEmail(),
                existing.getRole().name()
        );

        LoginResponse response = new LoginResponse(
                token,
                existing.getEmail(),
                existing.getRole().name(),
                existing.getPublicId(),
                existing.getName()
        );

        return ResponseEntity.ok(response);
    }
    public String generatePublicId(Role role) {

        String prefix = "";

        switch (role) {
            case ROLE_STUDENT -> prefix = "STU";
            case ROLE_RECRUITER -> prefix = "REC";
            case ROLE_ADMIN -> prefix = "ADM";
        }

        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }


    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {

        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        User user = vt.getUser();

        // check expiry
        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Token expired. Please resend verification email."
                    ));
        }

        user.setEnabled(true);
        userRepository.save(user);

        // optional: delete token after success
        verificationTokenRepository.delete(vt);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));

}
    // ================= RESEND EMAIL =================
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "User already verified"));
        }

        // delete old tokens
        verificationTokenRepository.deleteByUser(user);

        // new token
        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now().plusHours(24));

        verificationTokenRepository.save(vt);

        String link =
                "http://localhost:8080/job-portal/auth/verify-email?token=" + token;

        emailService.sendEmail(
                user.getEmail(),
                "Resend: Verify Your Email",
                "Click here to verify:\n" + link
        );

        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }
}