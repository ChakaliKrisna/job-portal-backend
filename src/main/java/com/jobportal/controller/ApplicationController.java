package com.jobportal.controller;

import com.jobportal.dto.*;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/job-portal/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService service;

    @Autowired
    private UserRepository userRepo;

    // =========================================================
    // ✅ STUDENT: APPLY JOB
    // =========================================================
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> apply(
            @PathVariable String jobId,
            @RequestBody ApplyRequest request
    ) {
        service.apply(jobId, request);
        return ResponseEntity.ok("Applied successfully");
    }

    // =========================================================
    // ✅ STUDENT: MY APPLICATIONS
    // =========================================================
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<ApplicationResponse>> myApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getMyApplications(page, size));
    }

    // =========================================================
    // ✅ STUDENT: CHECK APPLICATION STATUS
    // =========================================================
    @GetMapping("/check/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Boolean> checkApplied(@PathVariable String jobId) {
        return ResponseEntity.ok(service.isAlreadyApplied(jobId));
    }

    // =========================================================
    // ✅ STUDENT: MISSING SKILLS BEFORE APPLY
    // =========================================================
    @GetMapping("/job/{jobId}/missing-skills")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<String>> getMissingSkills(@PathVariable String jobId) {
        return ResponseEntity.ok(service.getMissingSkillsForApplicationBeforeApply(jobId));
    }

    // =========================================================
    // ✅ STUDENT: SINGLE APPLICATION DETAILS
    // =========================================================
    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationDetailsDto> getApplication(
            @PathVariable String applicationId,
            Authentication authentication
    ) {
        User student = userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                service.getApplicationDetails(applicationId, student)
        );
    }

    // =========================================================
    // ✅ RECRUITER: JOB APPLICATIONS (SECURE)
    // =========================================================
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationCandidateResponse>> getApplicationsByJob(
            @PathVariable String jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                service.getApplicationsByJobForRecruiter(
                        jobId,
                        authentication.getName(),
                        page,
                        size
                )
        );
    }

    // =========================================================
    // ✅ RECRUITER: UPDATE APPLICATION STATUS
    // =========================================================
    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<String> updateStatus(
            @PathVariable String applicationId,
            @RequestParam ApplicationStatus status
    ) {
        service.updateStatus(applicationId, status);
        return ResponseEntity.ok("Updated");
    }

    // =========================================================
    // ✅ RECRUITER: GLOBAL FILTER
    // =========================================================
    @GetMapping("/filter")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationCandidateResponse>> filterGlobal(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.filterCandidatesGlobal(keyword, minScore, status, skill, page, size)
        );
    }

    // =========================================================
    // ✅ RECRUITER: FILTER BY JOB
    // =========================================================
    @GetMapping("/job/{jobId}/filter")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationCandidateResponse>> filterByJob(
            @PathVariable String jobId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.filterCandidatesByJob(
                        jobId,
                        keyword,
                        minScore,
                        status,
                        skill,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // ✅ RECRUITER: MY JOBS
    // =========================================================
    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponseDTO>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getRecruiterJobs(page, size));
    }


    @GetMapping("/dashboard/analytics")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<RecruiterAnalyticsDTO> getDashboardAnalytics(Authentication auth) {

        String email = auth.getName();
        return ResponseEntity.ok(service.getRecruiterAnalytics(email));
    }

    @GetMapping("/dashboard/platform-overview")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<PlatformStatsDTO> getPlatformStats() {
        return ResponseEntity.ok(service.getPlatformStats());
    }

    @GetMapping("/jobs/{jobId}/match-score")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Double> getMatchScore(
            @PathVariable String jobId
    ) {
        return ResponseEntity.ok(
                service.calculateMatchScore(jobId, Collections.emptyList())
        );
    }
}