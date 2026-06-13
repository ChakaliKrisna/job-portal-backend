package com.jobportal.controller;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDetailsDto {

    private String applicationPublicId;

    private String jobTitle;

    private String companyName;

    private String status;

    private Double matchScore;

    private List<String> missingSkills;

    private String resumeUrl;

    private String coverLetter;

    private String availability;

    private String workPreference;

    private List<String> extraSkills;

    private LocalDateTime appliedAt;
}