package com.jobportal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyRequest {

    private String resumeUrl;

    private List<String> extraSkills;

    @NotBlank
    @Size(min = 20, max = 1000)
    private String coverLetter;

    private Boolean override;

    private String name;

    @Email
    private String email;
    private String availability;

    private String workPreference;
}