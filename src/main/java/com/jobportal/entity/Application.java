package com.jobportal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
        },
        indexes = {
                @Index(name = "idx_public_id", columnList = "publicId"),
                @Index(name = "idx_candidate", columnList = "candidate_id"),
                @Index(name = "idx_job", columnList = "job_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String publicId;
    private String candidateName;
    private String candidateEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    private Double matchScore;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(nullable = false) // if resume mandatory
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(nullable = false)
    private Boolean viewed = false;

    private String recruiterNotes;

    @PrePersist
    public void onCreate() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString();
        }
        this.appliedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
//    @ElementCollection
//    @CollectionTable(name = "application_skills", joinColumns = @JoinColumn(name = "application_id"))
//    @Column(name = "skill")
//    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "application_skills", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "skill")
    private List<String> skillsSnapshot;

    @ElementCollection
    @CollectionTable(name = "application_extra_skills", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "skill")
    private List<String> extraSkills;

    private String availability;

    private String workPreference;
    @Column(columnDefinition = "TEXT")
    private String resumeText;


//    @ElementCollection
//    @CollectionTable(name = "application_extra_skills", joinColumns = @JoinColumn(name = "application_id"))
//    @Column(name = "skill")
//    private List<String> extraSkills;


//    public void setSkillsSnapshot(List<String> skills) {
//    }
}