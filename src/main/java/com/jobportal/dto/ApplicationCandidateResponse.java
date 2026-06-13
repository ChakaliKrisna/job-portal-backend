package com.jobportal.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApplicationCandidateResponse {

    private String applicationId;
    private String studentPublicId;
    private String candidateName;
    private String email;
    private String jobPublicId;
    private String jobTitle;
    private String companyName;
    private String status;
    private Double matchScore;

    private List<String> skills;
    private List<String> extraSkills;

    private String resumeUrl;
    private String coverLetter;

    private Boolean viewed;

    private LocalDateTime appliedAt;
//    private String jobPublicId;
//    private String jobTitle;
//    private String companyName;
}
