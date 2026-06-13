package com.jobportal.dto;

//package com.jobportal.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApplicationResponse {
    private String candidateName;
    private String candidateEmail;
    private String jobId;
    private String applicationId;
    private String jobTitle;
    private String companyName;

    private String status;
    private Double matchScore;

    private List<String> skills;
    private List<String> extraSkills;

    private String resumeUrl;
    private String coverLetter;

    private LocalDateTime appliedAt;
}