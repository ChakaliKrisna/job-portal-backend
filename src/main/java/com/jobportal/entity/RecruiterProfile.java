package com.jobportal.entity;

//package com.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RecruiterProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each recruiter has one profile
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ================= RECRUITER INFO =================
    private String jobTitle;        // HR, Hiring Manager, Founder
    private String phoneNumber;
    private String linkedInUrl;

    // Optional personal info
    private String bio;
}