package com.jobportal.dto;

//package com.jobportal.dto;

import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//package com.jobportal.dto;

import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ApplicationDTO {

    private String publicId;

    private String jobTitle;
    private String companyName;

    private String candidateName;
    private String candidateEmail;

    private ApplicationStatus status;

    private Double matchScore;

    private List<String> skills;
    private List<String> extraSkills;

    private String resumeUrl;
    private String coverLetter;

    private Boolean viewed;

    private LocalDateTime appliedAt;

    // ✅ STATIC MAPPER
    public static ApplicationDTO toDTO(Application app) {

        ApplicationDTO dto = new ApplicationDTO();

        dto.setPublicId(app.getPublicId());

        // Job Info
        dto.setJobTitle(
                app.getJob() != null ? app.getJob().getTitle() : null
        );

        dto.setCompanyName(
                app.getJob() != null && app.getJob().getCompany() != null
                        ? app.getJob().getCompany().getName()
                        : null
        );

        // ✅ Candidate Info (SAFE + SNAPSHOT)
        dto.setCandidateName(
                app.getCandidateName() != null
                        ? app.getCandidateName()
                        : (app.getCandidate() != null ? app.getCandidate().getName() : null)
        );
        dto.setCandidateEmail(
                app.getCandidateEmail() != null
                        ? app.getCandidateEmail()
                        : (app.getCandidate() != null ? app.getCandidate().getEmail() : null)
        );

        dto.setCandidateEmail(
                app.getCandidateEmail() != null
                        ? app.getCandidateEmail()
                        : (app.getCandidate() != null ? app.getCandidate().getEmail() : null)
        );

        // Status
        dto.setStatus(app.getStatus());

        // Score
        dto.setMatchScore(app.getMatchScore());

        // Skills
        dto.setSkills(app.getSkillsSnapshot());
        dto.setExtraSkills(app.getExtraSkills());

        // Resume & Cover
        dto.setResumeUrl(app.getResumeUrl());
        dto.setCoverLetter(app.getCoverLetter());

        // Viewed
        dto.setViewed(app.getViewed());

        // Time
        dto.setAppliedAt(app.getAppliedAt());
        assert app.getCandidate() != null;
        dto.setPublicId(app.getCandidate().getPublicId());

        return dto;
    }
}