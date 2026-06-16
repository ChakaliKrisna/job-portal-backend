package com.jobportal.dto;

import com.jobportal.entity.JobCategory;
import com.jobportal.entity.JobSkill;
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

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobResponseDTO {

    private String publicId;
    private String title;
    private String location;
    private Double salary;

    private String jobType;
    private String workMode;
    private String experienceLevel;
    private String status;
    private String category;

    private String description;
    private Integer openings;

    private LocalDateTime postedDate;
    private LocalDateTime closedDate;

    private CompanyDTO company;
    private RecruiterDTO recruiter;
}