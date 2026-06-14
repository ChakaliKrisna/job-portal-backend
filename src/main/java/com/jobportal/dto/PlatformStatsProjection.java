package com.jobportal.dto;

public record PlatformStatsProjection(
        long totalUsers,
        long totalRecruiters,
        long totalStudents,
        long totalJobs,
        long totalApplications,
        long activeJobs
) {}