package com.jobportal.service;

import com.jobportal.controller.ApplicationDetailsDto;
import com.jobportal.dto.*;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

//package com.jobportal.service;


public interface ApplicationService {

    void apply(String jobPublicId, ApplyRequest request);

    Page<ApplicationResponse> getMyApplications(int page, int size);

    Page<ApplicationCandidateResponse> getApplicationsByJob(String jobPublicId, int page, int size);

    Optional<Application> findByPublicId(String publicId);

    void updateStatus(String publicId, ApplicationStatus status);
//    List<String> getMissingSkills(String jobPublicId);
    boolean isAlreadyApplied(String jobId);
//    public Double calculateMatchScore(String jobId, List<String> extraSkills)
    Double calculateMatchScore(String jobId, List<String> extraSkills);

    List<String> getMissingSkillsForApplicationBeforeApply(String jobId);

    ApplicationResponse getApplicationById(String applicationId);

//    List<String> getMissingSkills(String jobId);

    List<String> getMissingSkillsByApplication(String applicationId);

    ApplicationResponse getMyApplication(String jobId);
    public ApplicationDetailsDto getApplicationDetails(
            String applicationId,
            User student
    );
//    public Page<ApplicationCandidateResponse> filterCandidates(
//            String jobId,
//            String keyword,
//            Double minScore,
//            String status,
//            String skill,
//            int page,
//            int size
//    );
    public Page<ApplicationCandidateResponse> filterCandidatesByJob(

            String jobId,
            String keyword,
            Double minScore,
            String status,
            String skill,
            int page,
            int size
    );
    public Page<ApplicationCandidateResponse> filterCandidatesGlobal(

            String keyword,
            Double minScore,
            String status,
            String skill,
            int page,
            int size
    );

    Page<JobResponseDTO> getRecruiterJobs(int page, int size);

    Page<ApplicationCandidateResponse> getApplicationsByJobForRecruiter(String jobId, String name, int page, int size);

    RecruiterAnalyticsDTO getRecruiterAnalytics(String email);

    PlatformStatsDTO getPlatformStats();

//    Page<JobResponseDTO> getRecruiterJobs(int page, int size);
}