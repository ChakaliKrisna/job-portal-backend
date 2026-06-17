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

    private JobCategory category;
    private JobType jobType;
    private WorkMode workMode;
    private ExperienceLevel experienceLevel;

    private List<String> skillsRequired;
    private String education;

    private Integer openings;

    private LocalDateTime closingDate;

    private JobStatus status; // optional
}