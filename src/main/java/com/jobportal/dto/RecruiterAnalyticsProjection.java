package com.jobportal.dto;

public record RecruiterAnalyticsProjection(
        long totalJobs,
        long activeJobs,
        long totalApplications,
        long shortlisted,
        long interviews
) {}