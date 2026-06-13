package com.jobportal.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectResponse {

    private String title;
    private String description;
    private String techStack;
    private String githubLink;
}