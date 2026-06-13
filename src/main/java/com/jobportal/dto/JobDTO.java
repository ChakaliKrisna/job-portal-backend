package com.jobportal.dto;

import com.jobportal.entity.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.jobportal.entity.JobType;
import com.jobportal.entity.ExperienceLevel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor



public class JobDTO {

    private String title;
    private String companyName;
    private String location;
    private String description;
    private Double salary;

    // ✅ ENUMS
    private JobCategory category;        // 🔥 ADDED
    private JobType jobType;
    private WorkMode workMode;
    private ExperienceLevel experienceLevel;

    // ✅ Job Details
    private List<String> skillsRequired;
    private String education;

    private Integer openings;

    // ✅ Dates
//    private LocalDateTime PostedDate;
    private LocalDateTime closingDate;
}