package com.jobportal.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ProjectRequest {
    private String title;
    private String description;
    private String techStack;
    private String githubLink;


}
