package com.jobportal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/// Best practice: explicitly define lower-case plural table names for SQL compatibility
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "job", indexes = {
        @Index(name = "idx_job_public_id", columnList = "publicId")
})
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // INTERNAL ONLY ❌ never expose

    // ⭐ PUBLIC ID (USED IN API)
    @Column(unique = true, nullable = false, length = 50)
    private String publicId;

    // 🔹 BASIC DETAILS
    @Column(nullable = false)
    private String title;

    // Fix 1: Added JsonIgnoreProperties to prevent infinite serialization loop if Company references Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    @JsonIgnore
    private User recruiter;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double salary;

    // 🔹 REQUIREMENTS
    // Fix 2: Changed fetch type to LAZY (default for OneToMany) but we handle it via EntityGraph in the repo.
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("job") // Prevents bidirectional JSON infinite loops
    private List<JobSkill> skillsRequired = new ArrayList<>();

    private String education;

    // 🔹 JOB INFO
    private Integer openings;

    private LocalDateTime postedDate;

    private LocalDateTime closingDate; // Kept consistent with standard database naming conventions

    private Integer applicantsCount = 0;

    // 🔹 ENUMS (Using explicitly sized VARCHAR lengths for DB tuning metrics)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private JobCategory category;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Application> applications = new ArrayList<>();

    // Helper method to synchronize bidirectional synchronization updates seamlessly
    public void addSkill(JobSkill skill) {
        skillsRequired.add(skill);
        skill.setJob(this);
    }

    public void removeSkill(JobSkill skill) {
        skillsRequired.remove(skill);
        skill.setJob(null);
    }
}