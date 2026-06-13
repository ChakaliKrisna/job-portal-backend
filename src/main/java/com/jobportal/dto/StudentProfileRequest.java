package com.jobportal.dto;

//package com.jobportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

//package com.jobportal.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentProfileRequest {

    // ================= BASIC INFO =================
    private String phoneNumber;
    private String location;
    private String headline;

    // ================= ACADEMIC & SKILLS =================
    private List<String> skills;
    private String education;
    private String experience;

    // ================= RESUME =================
    private String resumeUrl;
    private String resumeFileName;

    // ================= PROJECTS =================
    private List<ProjectRequest> projects;

    // ================= LINKS =================
    private String githubUrl;
    private String linkedinUrl;

    // ================= EXTRAS =================
    private String achievements;

    // NOTE:
    // profileImageUrl NOT needed here
    // because you are handling it via file upload API
}