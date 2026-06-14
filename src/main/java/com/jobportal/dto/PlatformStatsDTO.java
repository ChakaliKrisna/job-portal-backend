package com.jobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlatformStatsDTO {

    private long totalUsers;
    private long totalRecruiters;
    private long totalStudents;
    private long totalJobs;
    private long totalApplications;
    private long activeJobs;
}