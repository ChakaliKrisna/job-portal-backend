package com.jobportal.service;

import com.jobportal.AccessDeniedException;
import com.jobportal.dto.*;
import com.jobportal.entity.*;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;

import com.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
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
    @Cacheable(value = "job", key = "#publicId", unless = "#result == null")
    public JobResponseDTO getJobByPublicId(String publicId) {

        Job job = jobRepo.findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job not found with id: " + publicId));

        return convertToDTO(job);
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

        JobResponseDTO dto = new JobResponseDTO();



        dto.setPublicId(job.getPublicId());
        dto.setTitle(job.getTitle());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());

        dto.setJobType(enumName(job.getJobType()));
        dto.setWorkMode(enumName(job.getWorkMode()));
        dto.setExperienceLevel(enumName(job.getExperienceLevel()));
        dto.setStatus(enumName(job.getStatus()));
        dto.setCategory(enumName(job.getCategory()));

        dto.setDescription(job.getDescription());
        dto.setOpenings(job.getOpenings());
        dto.setPostedDate(job.getPostedDate());
        dto.setClosedDate(job.getClosingDate());
        dto.setApplicantsCount(job.getApplicantsCount());

        dto.setCompany(toCompanyDTO(job.getCompany()));
        dto.setRecruiter(toRecruiterDTO(job.getRecruiter()));

        return dto;
    }
    private CompanyDTO toCompanyDTO(Company company) {
        if (company == null) return null;

        CompanyDTO dto = new CompanyDTO();
        dto.setPublicId(company.getPublicId());
        dto.setName(company.getName());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setWebsite(company.getWebsite());
        dto.setLocation(company.getLocation());

        return dto;
    }
    private String enumName(Enum<?> e) {
        return e != null ? e.name() : null;
    }
    private RecruiterDTO toRecruiterDTO(User recruiter) {
        if (recruiter == null) return null;

        return new RecruiterDTO(
                recruiter.getPublicId(),
                recruiter.getName(),
                recruiter.getEmail()
        );
    }
    @Override
    public Slice<JobCardDTO> getAllJobs( // ✅ Return type changed to Slice
                                         String keyword,
                                         String location,
                                         JobType jobType,
                                         WorkMode workMode,
                                         ExperienceLevel experienceLevel,
                                         JobStatus jobStatus,
                                         Double minSalary,
                                         JobCategory category,
                                         Pageable pageable) {

        // Clean inputs to avoid whitespace mismatches
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanLocation = (location != null && !location.trim().isEmpty()) ? location.trim() : null;

        // ✅ jobRepo now returns a Slice<Job> instead of a Page<Job>
        Slice<Job> jobsSlice = jobRepo.findFilteredJobs(
                jobStatus,
                jobType,
                workMode,
                experienceLevel,
                category,
                minSalary,
                cleanLocation,
                cleanKeyword,
                pageable
        );

        // ✅ Map it seamlessly to your DTO
        return jobsSlice.map(this::convertToCardDTO);
    }
//    Override
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

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User recruiter = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Job> jobs = jobRepo.findMyJobs(
                recruiter.getId(),
                keyword,
                location,
                jobType,
                workMode,
                experienceLevel,
                status,
                category,
                minSalary,
                pageable
        );

        List<JobResponseDTO> dtoList = jobs.stream()
                .map(this::convertToDTO)
                .toList();

        return new PageImpl<>(dtoList, pageable, dtoList.size());
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
    public Page<JobResponseDTO> getRecruiterJobs(int page, int size) {

        User recruiter = getLoggedInUser();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("postedDate").descending()
        );

        return jobRepo.findByRecruiter(recruiter, pageable)
                .map(this::convertToDTO);
    }
    private User getLoggedInUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }
    private JobCardDTO convertToCardDTO(Job job) {
        // Gracefully fallback to defaults if company data is missing
        String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "Anonymous Enterprise";
        String companyLogo = (job.getCompany() != null) ? job.getCompany().getLogoUrl() : null;

        return new JobCardDTO(
                job.getPublicId(),
                job.getTitle(),
                job.getLocation(),
                job.getSalary(),
                job.getJobType() != null ? job.getJobType().name() : null,
                job.getWorkMode() != null ? job.getWorkMode().name() : null,
                companyName,      // Safe from NullPointerExceptions
                companyLogo,      // Safe from NullPointerExceptions
                job.getOpenings(),
                job.getApplicantsCount()
        );
    }
    }





