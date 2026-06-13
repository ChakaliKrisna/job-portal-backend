package com.jobportal.controller;

//package com.jobportal.controller;

import com.jobportal.dto.JobResponseDTO;
import com.jobportal.entity.SavedJob;
import com.jobportal.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/job-portal/saved")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class SavedJobController {

    private final SavedJobService service;

    // ✅ SAVE JOB
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String saveJob(@PathVariable String jobId, Authentication auth) {
        return service.saveJob(jobId, auth.getName());
    }

    // ✅ UNSAVE JOB
    @Transactional
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String unsaveJob(@PathVariable String jobId, Authentication auth) {
        return service.unsaveJob(jobId, auth.getName());
    }

    // ✅ GET SAVED JOBS
//    @GetMapping
//    public List<SavedJob> getSavedJobs(Authentication auth) {
//        return service.getSavedJobs(auth.getName());
//    }

    // ✅ CHECK SAVED STATUS
    @GetMapping("/check/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    public boolean isSaved(@PathVariable String jobId, Authentication auth) {
        return service.isJobSaved(jobId, auth.getName());
    }
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public List<JobResponseDTO> getSavedJobs(Authentication auth) {
        return service.getSavedJobs(auth.getName());
    }
}