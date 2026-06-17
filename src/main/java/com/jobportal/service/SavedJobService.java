package com.jobportal.service;

//package com.jobportal.service;

import com.jobportal.dto.JobCardDTO;
import com.jobportal.dto.JobResponseDTO;
import com.jobportal.dto.RecruiterDTO;
import com.jobportal.entity.*;
import com.jobportal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepo;
    private final JobRepository jobRepo;
    private final UserRepository userRepo;

    // ✅ SAVE JOB
    public String saveJob(String jobId, String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (savedJobRepo.existsByUserAndJob(user, job)) {
            return "Job already saved";
        }

        SavedJob savedJob = SavedJob.builder()
                .user(user)
                .job(job)
                .savedAt(LocalDateTime.now())
                .build();

        savedJobRepo.save(savedJob);
        return "Job saved successfully";
    }

    // ✅ UNSAVE JOB
    public String unsaveJob(String jobId, String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        savedJobRepo.deleteByUserAndJob(user, job);
        return "Job removed from saved list";
    }


    // ✅ CHECK IF SAVED (for UI toggle)
    public boolean isJobSaved(String jobId, String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        Job job = jobRepo.findByPublicId(jobId).orElseThrow();

        return savedJobRepo.existsByUserAndJob(user, job);
    }
    public List<JobCardDTO> getSavedJobs(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return savedJobRepo.findByUser(user)
                .stream()
                .map(savedJob -> {

                    Job job = savedJob.getJob();

                    return new JobCardDTO(
                            job.getPublicId(),
                            job.getTitle(),
                            job.getLocation(),
                            job.getSalary(),
                            job.getJobType().name(),
                            job.getWorkMode().name(),
                            job.getCompany() != null
                                    ? job.getCompany().getName()
                                    : null,
                            job.getCompany() != null
                                    ? job.getCompany().getLogoUrl()
                                    : null,
                            job.getOpenings(),
                            job.getApplicantsCount()
                    );
                })
                .toList();
    }}