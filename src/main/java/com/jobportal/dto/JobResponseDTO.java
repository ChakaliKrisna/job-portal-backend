package com.jobportal.dto;

import com.jobportal.dto.CompanyDTO;
import com.jobportal.dto.RecruiterDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
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
    private String education;     // Recommended
    private Integer openings;

    private LocalDateTime postedDate;
    private LocalDateTime closedDate;
    private Integer applicantsCount;

    private List<String> skillsRequired;

    private CompanyDTO company;
    private RecruiterDTO recruiter;
}