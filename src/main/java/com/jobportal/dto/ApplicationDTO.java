package com.jobportal.dto;

import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationSkill;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ApplicationDTO {

    private String publicId;

    private String jobTitle;
    private String companyName;

    private String candidateName;
    private String candidateEmail;

    private String status;

    private Double matchScore;

    private List<String> skills;

    private String resumeUrl;
    private String coverLetter;

    private Boolean viewed;

    private LocalDateTime appliedAt;

    // ===================== MAPPER =====================
    public static ApplicationDTO toDTO(Application app) {

        ApplicationDTO dto = new ApplicationDTO();

        // ================= Application ID =================
        dto.setPublicId(app.getPublicId());

        // ================= Job Info =================
        dto.setJobTitle(
                app.getJob() != null ? app.getJob().getTitle() : null
        );

        dto.setCompanyName(
                app.getJob() != null && app.getJob().getCompany() != null
                        ? app.getJob().getCompany().getName()
                        : null
        );

        // ================= Candidate Info =================
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

        // ================= Status =================
        dto.setStatus(
                app.getStatus() != null ? app.getStatus().name() : null
        );

        // ================= Match Score =================
        dto.setMatchScore(app.getMatchScore());

        // ================= SKILLS (FIXED) =================
        dto.setSkills(
                app.getSkills() != null
                        ? app.getSkills()
                        .stream()
                        .map(ApplicationSkill::getSkill)
                        .collect(Collectors.toList())
                        : List.of()
        );

        // ================= Resume & Cover =================
        dto.setResumeUrl(app.getResumeUrl());
        dto.setCoverLetter(app.getCoverLetter());

        // ================= Viewed =================
        dto.setViewed(app.getViewed());

        // ================= Applied Time =================
        dto.setAppliedAt(app.getAppliedAt());

        return dto;
    }
}