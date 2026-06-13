package com.jobportal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
//import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // INTERNAL ONLY ❌ never expose

    // ⭐ PUBLIC ID (USED IN API)
    @Column(unique = true, nullable = false)
    private String publicId;

    // 🔹 BASIC DETAILS
    private String title;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "recruiter_id")
    private User recruiter;


    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;
//    private String description;
    private Double salary;

    // 🔹 JOB TYPE
//    private String jobType;
//    private String workMode;
//    private String experienceLevel;

    // 🔹 REQUIREMENTS
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSkill> skillsRequired = new ArrayList<>();
    private String education;

    // 🔹 JOB INFO
    private Integer openings;   // ⭐ IMPORTANT FIX

    private LocalDateTime postedDate;

    private LocalDateTime closingDate;

    private Integer applicantsCount = 0;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    // ⭐ RECRUITER (VERY IMPORTANT)
//    @ManyToOne
//    @JoinColumn(name = "recruiter_id")
//    private User recruiter;
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Application> applications;


//    / ✅ ENUMS (IMPORTANT)
    @Enumerated(EnumType.STRING)
    private JobCategory category;



}