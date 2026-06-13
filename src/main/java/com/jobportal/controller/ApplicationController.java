package com.jobportal.controller;

import com.jobportal.dto.ApplicationCandidateResponse;
import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.ApplyRequest;
import com.jobportal.dto.JobResponseDTO;
import com.jobportal.entity.*;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.ApplicationService;
//import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

//package com.jobportal.controller;

import com.jobportal.entity.Application;
import com.jobportal.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-portal/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService service;
    @Autowired
    private UserRepository userRepo;

    // ================= APPLY JOB =================
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> apply(@PathVariable String jobId, @RequestBody ApplyRequest request) {
        service.apply(jobId,request);
        return ResponseEntity.ok("Applied successfully");
    }
    @GetMapping("/jobs/{jobId}/match-score")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Double> getMatchScore(
            @PathVariable String jobId,
            @RequestParam(required = false) List<String> extraSkills
    ) {
        return ResponseEntity.ok(
                service.calculateMatchScore(jobId, extraSkills)
        );
    }

    // ================= MY APPLICATIONS =================
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Page<ApplicationResponse>> myApps(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getMyApplications(page, size));
    }

    // ================= JOB APPLICATIONS =================
    @GetMapping("/job/{jobPublicId}")
    public ResponseEntity<Page<ApplicationCandidateResponse>> getApplicants(
            @PathVariable String jobPublicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.getApplicationsByJob(jobPublicId, page, size)
        );
    }

    // ================= UPDATE STATUS =================
    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
    public ResponseEntity<String> updateStatus(
            @PathVariable String publicId,
            @RequestParam ApplicationStatus status) {

        service.updateStatus(publicId, status);
        return ResponseEntity.ok("Updated");
    }
//    @GetMapping("/{applicationId}")
//    @PreAuthorize("hasRole('STUDENT')")
//    public ResponseEntity<ApplicationResponse> getApplicationById(
//            @PathVariable String applicationId
//    ) {
//        return ResponseEntity.ok(service.getApplicationById(applicationId));
//    }

    @GetMapping("/check/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Boolean> checkApplied(@PathVariable String jobId) {
        return ResponseEntity.ok(service.isAlreadyApplied(jobId));
    }

    @GetMapping("/jobs/{jobId}/missing-skills")
    @PreAuthorize("hasRole('STUDENT') ")
    public ResponseEntity<List<String>> getMissingSkillsBeforeApply(
            @PathVariable String jobId
    ) {

        return ResponseEntity.ok(service.getMissingSkillsForApplicationBeforeApply(jobId));
    }

    @GetMapping("/{applicationId}/missing-skills")
//    @PreAuthorize("hasAnyRole('STUDENT','RECRUITER')")
    public ResponseEntity<List<String>> getMissingSkillsByApplication(
            @PathVariable String applicationId
    ) {
        System.out.println("hitting missing skills ");
        return ResponseEntity.ok(
                service.getMissingSkillsByApplication(applicationId)
        );
    }
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/{jobId}/my-application")
    public ResponseEntity<ApplicationResponse> getMyApplication(@PathVariable String jobId) {
        return ResponseEntity.ok(service.getMyApplication(jobId));
    }


    @GetMapping("/{applicationId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApplicationDetailsDto> getApplication(
            @PathVariable String applicationId,
            Authentication authentication
    ) {

//        UserRepository userRepo;
        User student = userRepo
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                service.getApplicationDetails(applicationId, student)
        );
    }

    @GetMapping("/by-job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationCandidateResponse>> getApplicationsByJob(@PathVariable String JobPublicId, @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
                Page<ApplicationCandidateResponse> response= service.getApplicationsByJob(JobPublicId,page,size);

        return ResponseEntity.ok(response);
    }
    // =========================================================
// ✅ GLOBAL RECRUITER FILTER
// Search across ALL applications
// =========================================================



        // =========================================================
        // ✅ GLOBAL FILTER
        // =========================================================

        @GetMapping("/filter")
        @PreAuthorize("hasRole('RECRUITER')")
        public ResponseEntity<Page<ApplicationCandidateResponse>>
        filterCandidatesGlobal(

                @RequestParam(required = false) String keyword,

                @RequestParam(required = false) Double minScore,

                @RequestParam(required = false) String status,

                @RequestParam(required = false) String skill,

                @RequestParam(defaultValue = "0") int page,

                @RequestParam(defaultValue = "10") int size
        ) {

            return ResponseEntity.ok(

                    service.filterCandidatesGlobal(
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
        // ✅ FILTER BY JOB
        // =========================================================

        @GetMapping("/job/{jobId}/filter")
        @PreAuthorize("hasRole('RECRUITER')")
        public ResponseEntity<Page<ApplicationCandidateResponse>>
        filterCandidatesByJob(

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
    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobResponseDTO>> getMyJobs(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size
    ) {

        return ResponseEntity.ok(
                service.getRecruiterJobs(page, size)
        );
    }

    }