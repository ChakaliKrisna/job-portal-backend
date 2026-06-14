package com.jobportal.service;

import com.jobportal.AccessDeniedException;
import com.jobportal.dto.JobDTO;
import com.jobportal.dto.JobResponseDTO;
import com.jobportal.dto.RecruiterDTO;
import com.jobportal.entity.*;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;

import com.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
//import com.jobportal.specification.JobSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private CompanyRepository companyRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JobRepository jobRepo;

    public JobResponseDTO createJob(JobDTO dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User recruiter = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!recruiter.getRole().name().equals("ROLE_RECRUITER")) {
            throw new AccessDeniedException("Only recruiters can create jobs");
        }

        Company company = recruiter.getCompany();

        if (company == null) {
            throw new BadRequestException("Recruiter must be linked to a company");
        }

        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setLocation(dto.getLocation());
        job.setDescription(dto.getDescription());
        job.setSalary(dto.getSalary());

        // ✅ ENUM FIXES
        job.setJobType(dto.getJobType()); // if DTO uses enum
        job.setWorkMode(dto.getWorkMode());
        job.setExperienceLevel(dto.getExperienceLevel());
        job.setStatus(JobStatus.OPEN);

        job.setSkillsRequired(
                dto.getSkillsRequired()
                        .stream()
                        .map(skill -> {
                            JobSkill js = new JobSkill();
                            js.setSkill(skill);
                            js.setJob(job); // VERY IMPORTANT (bi-directional mapping)
                            return js;
                        })
                        .toList()
        );
        job.setEducation(dto.getEducation());
        job.setClosingDate(dto.getClosingDate());
        job.setPostedDate(LocalDateTime.now());
        job.setPublicId("JOB_" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        job.setOpenings(dto.getOpenings());

        job.setCompany(company);
        job.setRecruiter(recruiter);
        job.setCategory(dto.getCategory());

        return convertToDTO(jobRepo.save(job));

//
//        String location,
//        JobType jobType,
//        WorkMode workMode,
//        String keyword,
//        JobCategory category,
//        Pageable pageable
    }



    @Override
    public JobResponseDTO getJobByPublicId(String publicId) {

        Job job = jobRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + publicId));

        return convertToDTO(job); // ✅ convert entity → DTO
    }

    @Override
    public JobResponseDTO updateJob(String publicId, JobDTO dto) {

        // 🔐 Get logged-in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // 🔐 ROLE CHECK (do early)
        if (!"ROLE_RECRUITER".equals(currentUser.getRole().name())) {
            throw new AccessDeniedException("Access denied: Only recruiters can update jobs");
        }

        // 🔍 Fetch job using publicId
        Job job = jobRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with publicId: " + publicId));

        // 🔐 OWNER CHECK
        if (!job.getRecruiter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You can only update your own jobs");
        }

        System.out.println("Logged in email = " + email);
        System.out.println("Current User ID = " + currentUser.getId());
        System.out.println("Current User Role = " + currentUser.getRole());

        System.out.println("Job Recruiter ID = " + job.getRecruiter().getId());
        System.out.println("Job Recruiter Email = " + job.getRecruiter().getEmail());


        // ⚠️ Optional: Check company
        if (currentUser.getCompany() == null) {
            throw new BadRequestException("Recruiter must be associated with a company to update jobs");
        }

        // ✏️ Update fields (only allowed fields)
        job.setTitle(dto.getTitle());
        job.setLocation(dto.getLocation());
        job.setDescription(dto.getDescription());
        job.setSalary(dto.getSalary());
        job.setJobType(dto.getJobType());
        job.setWorkMode(dto.getWorkMode());
        job.setExperienceLevel(dto.getExperienceLevel());
        // get existing managed collection
        List<JobSkill> existingSkills = job.getSkillsRequired();

// clear old entries (Hibernate will handle orphan delete)
        existingSkills.clear();

// add new ones
        List<JobSkill> newSkills = dto.getSkillsRequired()
                .stream()
                .map(skill -> {
                    JobSkill js = new JobSkill();
                    js.setSkill(skill);
                    js.setJob(job);
                    return js;
                })
                .toList();

        existingSkills.addAll(newSkills);
        job.setEducation(dto.getEducation());
        job.setOpenings(dto.getOpenings());
        job.setClosingDate(dto.getClosingDate());
        job.setCategory(dto.getCategory());

        // 💾 Save updated job
        Job updatedJob = jobRepo.save(job);

        // 🔄 Return DTO (best practice)
        return convertToDTO(updatedJob);
    }

    @Override
    public void deleteJob(String publicId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getRecruiter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this job");
        }

        if (!currentUser.getRole().name().equals("ROLE_RECRUITER")) {
            throw new AccessDeniedException("Only recruiters can delete jobs");
        }

        jobRepo.delete(job);
    }

//    @Override
//    public Page<JobResponseDTO> getMyJobs(String keyword, Pageable pageable) {
//        return null;
//    }

//    @Override
//    public Page<JobResponseDTO> getMyJobs(String keyword, Pageable pageable) {
//        return null;
//    }


//    @Override
//    public Page<JobResponseDTO> getMyJobs(String keyword, Pageable pageable) {
//        return null;
//    }

    public JobResponseDTO convertToDTO(Job job) {

        RecruiterDTO recruiterDTO = null;

        if (job.getRecruiter() != null) {
            recruiterDTO = new RecruiterDTO(
                    job.getRecruiter().getPublicId(),
                    job.getRecruiter().getName(),
                    job.getRecruiter().getEmail()
            );
        }

        return new JobResponseDTO(job, recruiterDTO);
    }

    @Override
    public Page<Job> getAllJobs(String keyword, String location, JobType jobType, WorkMode workMode, ExperienceLevel experienceLevel, JobStatus JobStatus, Double minSalary, JobCategory category, Pageable pageable) {
        Specification<Job> spec = Specification
                .where(JobSpecification.searchKeyword(keyword))
                .and(JobSpecification.hasLocation(location))
                .and(JobSpecification.hasJobType(jobType))           // ✅ FIX
                .and(JobSpecification.hasWorkMode(workMode))         // ✅ FIX
                .and(JobSpecification.hasExperienceLevel(experienceLevel)) // ✅ FIX
                .and(JobSpecification.hasStatus(JobStatus))             // ✅ FIX
                .and(JobSpecification.hasCategory(category))         // ✅ ADD THIS
                .and(JobSpecification.hasMinSalary(minSalary));
        return jobRepo.findAll(spec, pageable);
    }

//
//    @Override
//    public <T> Optional<T> getAllJobs(String keyword, String location, JobType jobType, WorkMode workMode, ExperienceLevel experienceLevel, JobStatus jobStatus, double v, JobCategory category, Pageable pageable) {
//        return Optional.empty();
//    }

//    @Override
//    public Page<JobResponseDTO> getMyJobs(String keyword, String location, String jobType, String workMode, String experienceLevel, String status, Double minSalary, Pageable pageable) {
//        return null;
//    }

    @Override
    public Page<JobResponseDTO> getMyJobs(
            String keyword,
            String location,
            JobType jobType,
            WorkMode workMode,
            ExperienceLevel experienceLevel,
            JobStatus status,
            Double minSalary,
            JobCategory category,
            Pageable pageable) {

        // 🔐 Get logged-in user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User recruiter = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🔐 Role check
        if (!"ROLE_RECRUITER".equals(recruiter.getRole().name())) {
            throw new AccessDeniedException("Only recruiters can view their jobs");
        }

        // 🔍 Build Specification
        Specification<Job> spec = Specification
                .where(JobSpecification.belongsToRecruiter(recruiter.getId()))
                .and(JobSpecification.searchKeyword(keyword))
                .and(JobSpecification.hasLocation(location))
                .and(JobSpecification.hasJobType(jobType))           // ✅ FIX
                .and(JobSpecification.hasWorkMode(workMode))         // ✅ FIX
                .and(JobSpecification.hasExperienceLevel(experienceLevel)) // ✅ FIX
                .and(JobSpecification.hasStatus(status))             // ✅ FIX
                .and(JobSpecification.hasCategory(category))         // ✅ ADD THIS
                .and(JobSpecification.hasMinSalary(minSalary));
        // 📦 Fetch data
        Page<Job> jobPage = jobRepo.findAll(spec, pageable);

        // 🔄 Convert to DTO
        return jobPage.map(this::convertToDTO);
    }

//    @Override
//    public Page<JobResponseDTO> getMyJobs(String keyword, String location, String jobType, String workMode, String experienceLevel, Pageable pageable) {
//        return null;
//    }


    public JobResponseDTO toggleJobStatus(String publicId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // 🔐 OWNER CHECK
        if (!job.getRecruiter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to update this job");
        }

        // 🔐 ROLE CHECK
        if (!currentUser.getRole().name().equals("ROLE_RECRUITER")) {
            throw new AccessDeniedException("Only recruiters can update jobs");
        }

        // 🔄 TOGGLE LOGIC
        if (job.getStatus() == JobStatus.OPEN) {
            job.setStatus(JobStatus.CLOSED);
        } else {
            job.setStatus(JobStatus.OPEN);
        }

        return convertToDTO(jobRepo.save(job));
    }

    @Override
    public JobResponseDTO getMyJobByPublicId(String publicId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Job job = jobRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // 🔐 OWNER CHECK
        if (!job.getRecruiter().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not allowed to view this job");
        }

        return convertToDTO(job);
    }
    public Page<JobResponseDTO> getJobsByCompany(
            String companyPublicId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("postedDate").descending()
        );

        Page<Job> jobs =
                jobRepo.findByCompany_PublicId(
                        companyPublicId,
                        pageable
                );

        return jobs.map(this::convertToDTO);
        }
    }




