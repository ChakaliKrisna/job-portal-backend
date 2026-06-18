package com.jobportal.controller;

////package controller;
//
//import dto.JobDTO;
//import entity.Job;
//import service.JobService;

import com.jobportal.dto.JobCardDTO;
import com.jobportal.dto.JobDTO;
import com.jobportal.dto.JobResponseDTO;
import com.jobportal.entity.*;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.service.JobService;
import com.jobportal.service.JobSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;


//@EnableMethodSecurity
@RestController
@RequestMapping("/job-portal/jobs")
//@CrossOrigin
public class JobController {

    @Autowired
    private JobService service;
    @Autowired
    private JobRepository jobRepo;
    @Autowired
    CompanyRepository companyRepository;

    // ✅ CREATE JOB
    @PreAuthorize("hasRole('RECRUITER')")
    @PostMapping
    public ResponseEntity<JobResponseDTO> createJob(@RequestBody JobDTO dto) {
        return ResponseEntity.ok(service.createJob(dto));
    }

    // ✅ GET ALL JOBS
//    @PreAuthorize("hasAnyRole('STUDENT','RECRUITER')")
//    @Override
    @GetMapping
    public ResponseEntity<Slice<JobCardDTO>> getAllJobs( // ✅ Return type changed to Slice

                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String location,
                                                         @RequestParam(required = false) JobType jobType,
                                                         @RequestParam(required = false) WorkMode workMode,
                                                         @RequestParam(required = false) ExperienceLevel experienceLevel,
                                                         @RequestParam(required = false) JobStatus jobStatus,
                                                         @RequestParam(required = false) Double minSalary,
                                                         @RequestParam(required = false) JobCategory category,
                                                         Pageable pageable) {

        return ResponseEntity.ok(
                service.getAllJobs(
                        keyword,
                        location,
                        jobType,
                        workMode,
                        experienceLevel,
                        jobStatus,
                        minSalary,
                        category,
                        pageable
                )
        );
    }
    // ✅ UPDATE JOB
    @PreAuthorize("hasRole('RECRUITER')")
    @PutMapping("/{publicId}")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable String publicId,
            @RequestBody JobDTO dto) {

        return ResponseEntity.ok(service.updateJob(publicId, dto));
    }

    // ✅ DELETE JOB
    @PreAuthorize("hasRole('RECRUITER')")
    @DeleteMapping("/{publicId}")
    public ResponseEntity<String> deleteJob(@PathVariable String publicId) {
        service.deleteJob(publicId);
        return ResponseEntity.ok("Job deleted successfully");
    }

    // ✅ MY JOBS
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/my-jobs")
    public Page<JobResponseDTO> getMyJobs(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) JobCategory category, // ✅ ADD THIS
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false)JobStatus jobStatus,


            @RequestParam(defaultValue = "postedDate,desc") String sort,

            Pageable pageable) {

        return service.getMyJobs(
                keyword,
                location,
                jobType,
                workMode,
                experienceLevel,
                jobStatus,
                minSalary != null ? minSalary : 0.0,
                category,
                pageable

        );
    }

    // ✅ TOGGLE STATUS
    @PreAuthorize("hasRole('RECRUITER')")
    @PatchMapping("/{publicId}/toggle-status")
    public ResponseEntity<JobResponseDTO> toggleJobStatus(@PathVariable String publicId) {
        return ResponseEntity.ok(service.toggleJobStatus(publicId));
    }

    // ✅ GET MY JOB BY PUBLIC ID
    @PreAuthorize("hasRole('RECRUITER')")
    @GetMapping("/my-jobs/{publicId}")
    public ResponseEntity<JobResponseDTO> getMyJobById(@PathVariable String publicId) {
        return ResponseEntity.ok(service.getMyJobByPublicId(publicId));
    }
    @GetMapping("/{publicId}")
//    @PreAuthorize("hasAnyRole('STUDENT','RECRUITER')")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable String publicId) {
        return ResponseEntity.ok(service.getJobByPublicId(publicId));
    }

    @PreAuthorize("hasAnyRole('STUDENT','RECRUITER')")
    @GetMapping("/companies/public/{publicId}")
    public ResponseEntity<Company> getCompanyByPublicId(@PathVariable String publicId) {

        Company company = companyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return ResponseEntity.ok(company);
    }

    @GetMapping("/company/{publicId}")
    public ResponseEntity<Page<JobResponseDTO>> getJobsByCompany(

            @PathVariable String publicId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return ResponseEntity.ok(
                service.getJobsByCompany(
                        publicId,
                        page,
                        size
                )
        );
    }

}