package com.jobportal.service;

import com.jobportal.dto.JobCardDTO;
import com.jobportal.dto.JobDTO;
import com.jobportal.dto.JobResponseDTO;
import com.jobportal.entity.*;
import jdk.jfr.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface JobService {

    JobResponseDTO createJob(JobDTO dto);


//    Job getJobByPublicId(String publicId);

    JobResponseDTO getJobByPublicId(String publicId);

//    Job updateJob(Long id, JobDTO dto);
    JobResponseDTO updateJob(String publicId, JobDTO dto);

    void deleteJob(String publicId);
//    Page<JobResponseDTO> getMyJobs(String keyword, Pageable pageable);

    Page<JobResponseDTO> getMyJobs(
            String keyword,
            String location,
            JobType jobType,
            WorkMode workMode,
            ExperienceLevel experienceLevel,
            JobStatus status,
            Double minSalary,
            JobCategory category,
            Pageable pageable);

    JobResponseDTO toggleJobStatus(String publicId);

    JobResponseDTO getMyJobByPublicId(String publicId);

    JobResponseDTO convertToDTO(Job job);

//    <T> Optional<T> getAllJobs(String keyword, String location, JobType jobType, WorkMode workMode, ExperienceLevel experienceLevel, JobStatus jobStatus, double v, JobCategory category, Pageable pageable);

//    Page<JobResponseDTO> getMyJobs(String keyword, String location, String jobType, String workMode, String experienceLevel,String status,Double minSalary, Pageable pageable);

//    Page<JobResponseDTO> getMyJobs(String keyword, String location, String jobType, String workMode, String experienceLevel, Pageable pageable);

//    JobResponseDTO convertToDTO(Job job);
public Page<JobCardDTO> getAllJobs(
        String keyword,
        String location,
        JobType jobType,
        WorkMode workMode,
        ExperienceLevel experienceLevel,
        JobStatus jobStatus,
        Double minSalary,
        JobCategory category,
        Pageable pageable);

    Page<JobResponseDTO> getJobsByCompany(String publicId, int page, int size);

    Page<JobResponseDTO> getRecruiterJobs(int page, int size);
//    location,
//    jobType,
//    workMode,
//    experienceLevel,
//    jobStatus,
//    minSalary != null ? minSalary : 0.0,
//    category,
//    pageable
}