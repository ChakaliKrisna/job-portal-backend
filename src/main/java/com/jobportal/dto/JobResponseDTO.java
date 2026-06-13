package com.jobportal.dto;

import com.jobportal.entity.JobCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
//package com.jobportal.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
//import com/jobportal/entity/Job.java
import com.jobportal.entity.Job;
@Getter

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobResponseDTO {

    private String publicId;
    private String title;
    private String company; // Company Name
    private String location;
    private Double salary;
    private String jobType;         // Enum.name()
    private String workMode;        // Enum.name()
    private String experienceLevel; // Enum.name()
    private List<String> skillsRequired;
    private String education;
    private Integer openings;
    private String status;          // Enum.name()
    private LocalDateTime postedDate;
    private LocalDateTime closedDate;
    private String category;        // Enum.name()
    private String description;
    private Long applicationsCount;
    private RecruiterDTO recruiter;
    private String companyPublicId;
    private String companyName;
    private String companyLogo;
    private String companyLocation;
    private String companyWebsite;
//    private Integer applicationsCount;

    // A clean constructor to use in your Service/Mapper
    public JobResponseDTO(Job job, RecruiterDTO recruiterDTO) {

        this.publicId = job.getPublicId();
        this.title = job.getTitle();

        if (job.getCompany() != null) {
            this.companyPublicId = job.getCompany().getPublicId();
            this.companyName = job.getCompany().getName();
            this.companyLogo = job.getCompany().getLogoUrl();
            this.companyLocation = job.getCompany().getLocation();
            this.companyWebsite = job.getCompany().getWebsite();
        }

        this.location = job.getLocation();
        this.salary = job.getSalary();

        this.jobType = job.getJobType() != null
                ? job.getJobType().name()
                : null;

        this.workMode = job.getWorkMode() != null
                ? job.getWorkMode().name()
                : null;

        this.experienceLevel = job.getExperienceLevel() != null
                ? job.getExperienceLevel().name()
                : null;

        this.status = job.getStatus() != null
                ? job.getStatus().name()
                : null;

        this.category = job.getCategory() != null
                ? job.getCategory().name()
                : null;

        this.skillsRequired = job.getSkillsRequired();
        this.education = job.getEducation();
        this.openings = job.getOpenings();
        this.postedDate = job.getPostedDate();
        this.closedDate = job.getClosingDate();
        this.description = job.getDescription();
        this.recruiter = recruiterDTO;

        this.applicationsCount =
                job.getApplications() != null
                        ? (long) job.getApplications().size()
                        : 0;
    }


}