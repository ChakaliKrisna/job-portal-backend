package com.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterAnalyticsDTO {

    private long totalJobs;
    private long activeJobs;
    private long totalApplications;
    private long shortlisted;
    private long interviews;
}