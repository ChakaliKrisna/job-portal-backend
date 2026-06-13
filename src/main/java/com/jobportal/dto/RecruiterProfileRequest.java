package com.jobportal.dto;

//package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecruiterProfileRequest {

    // ================= BASIC COMPANY INFO =================
    @NotBlank(message = "Company name is required")
    private String companyName;
    private String website;
    private String description;
    private String location;

    // ================= COMPANY DETAILS =================
    private String industry;        // IT, Finance, Healthcare, etc.
    private String companySize;     // 1-10, 10-50, 100+
    private String foundedYear;

    // ================= RECRUITER INFO =================
    private String jobTitle;        // HR, Hiring Manager, Founder
    private String phoneNumber;

    // ================= OPTIONAL BRANDING =================
    private String logoUrl;
    private String linkedInUrl;
}