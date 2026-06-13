package com.jobportal.controller;

import com.jobportal.dto.*;
import com.jobportal.entity.PasswordResetToken;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

//package com.jobportal.controller;

import org.springframework.security.core.Authentication;

//package com.jobportal.controller;

//ipackage com.jobportal.controller;

import com.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;

//package com.jobportal.controller;

import com.jobportal.dto.StudentProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
//    http://localhost:8080/api/users/student/profile/'

    private final UserService userService;
    private final UserRepository userRepo;

    // ================= STUDENT PROFILE UPDATE =================
    @PutMapping("/student/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> updateStudentProfile(
            @RequestBody StudentProfileRequest request,
            Authentication auth
    ) {
        userService.updateStudentProfile(auth.getName(), request);
        return ResponseEntity.ok("Student profile updated");
    }

    // ================= GET STUDENT PROFILE =================
    @GetMapping("/student/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> getStudentProfile(Authentication auth) {
        StudentProfileResponse profile = userService.getStudentProfile(auth.getName());

        if (profile == null) {
            return ResponseEntity.ok(null); // IMPORTANT for frontend
        }

        return ResponseEntity.ok(profile);
    }

    // ================= STUDENT PROFILE COMPLETION =================
    @GetMapping("/student/profile/completion")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Integer> studentCompletion(Authentication auth) {
        return ResponseEntity.ok(
                userService.getStudentProfileCompletion(auth.getName())
        );
    }

    // ================= RECRUITER PROFILE COMPLETION =================
    @GetMapping("/recruiter/profile/completion")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Integer> recruiterCompletion(Authentication auth) {
        return ResponseEntity.ok(
                userService.getRecruiterProfileCompletion(auth.getName())
        );
    }
    @PostMapping("/student/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> uploadFiles(
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "resume", required = false) MultipartFile resume,
            Authentication auth
    ) {

        String resumeUrl = userService.uploadStudentFiles(
                auth.getName(),
                profileImage,
                resume
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Files uploaded successfully",
                        "resumeUrl", resumeUrl
                )
        );
    }
    // =========================================================
// GET STUDENT PROFILE BY PUBLIC ID
// Recruiter can view candidate profile
// =========================================================

    @GetMapping("/student/{publicId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<StudentProfileResponse>
    getStudentProfileByPublicId(

            @PathVariable String publicId
    ) {

        return ResponseEntity.ok(
                userService.getStudentProfileByPublicId(publicId)
        );
    }

}