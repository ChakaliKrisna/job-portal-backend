package com.jobportal.dto;

import lombok.*;

import java.util.List;

@Getter

@Setter
@AllArgsConstructor
@NoArgsConstructor

public class StudentProfileResponse {

    private String publicId;

    private String name;

    private String email;

    private String phoneNumber;

    private String location;

    private String headline;

    private List<String> skills;

    private String education;

    private String experience;

    private String resumeUrl;

    private String githubUrl;

    private String linkedinUrl;

    private String profileImageUrl;

    private List<ProjectResponse> projects;

    private String achievements;
}