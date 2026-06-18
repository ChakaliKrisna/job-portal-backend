package com.jobportal.repository;

import com.jobportal.dto.JobResponseDTO;
import com.jobportal.dto.PlatformStatsProjection;
import com.jobportal.dto.RecruiterAnalyticsProjection;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidate(User candidate);

    List<Application> findByJob(Job job);

    Optional<Application> findByJobAndCandidate(Job job, User candidate);
//    Optional<Application> findByPublicId(String publicId);

    boolean existsByJobAndCandidate(Job job, User candidate);

//    Page<Application> findByCandidate(User user, Pageable pageable);

    Page<Application> findByJob(Job job, Pageable pageable);
//    Page<Application> findByCandidate(User candidate, Pageable pageable);
Page<Application> findByCandidateAndStatus(User user, ApplicationStatus status, Pageable pageable);

//    Optional<Object> findByJobPublicIdAndCandidate(String jobPublicId, User user);
//Optional<Application> findByJob_PublicIdAndCandidate(String jobPublicId, User candidate);

//    Optional<Object> findByJobPublicIdAndCandidate(String jobPublicId, User user);
//    boolean existsByJobAndCandidate(Job job, User candidate);

    Optional<Application> findByJob_PublicIdAndCandidate(String jobPublicId, User candidate);

    Optional<Object> findByJobPublicIdAndCandidate(String jobPublicId, User user);

    long countByJob(Job job);

    Page<Application> findByJobPublicId(String jobId, Pageable pageable);
//    Optional<Application> findByPublicId(String publicId);


    Page<Application> findByJob_PublicIdAndJob_Recruiter_Id(
            String jobId,
            Long recruiterId,
            Pageable pageable
    );
    Page<Application> findByJob_Recruiter_IdAndResumeTextContainingIgnoreCase(
            Long recruiterId,
            String keyword,
            Pageable pageable
    );

    Page<Application> findByJob_Recruiter_Id(Long id, Pageable pageable);

    long countByJob_Recruiter_IdAndStatus(Long recruiterId, ApplicationStatus applicationStatus);

    long countByJob_Recruiter_Id(Long recruiterId);

    @Query("""
SELECT new com.jobportal.dto.PlatformStatsProjection(
    (SELECT COUNT(u) FROM User u),
    (SELECT COUNT(u) FROM User u WHERE u.role='ROLE_RECRUITER'),
    (SELECT COUNT(u) FROM User u WHERE u.role='ROLE_STUDENT'),
    (SELECT COUNT(j) FROM Job j),
    (SELECT COUNT(a) FROM Application a),
    (SELECT COUNT(j) FROM Job j WHERE j.status='OPEN')
)
FROM User u
WHERE u.id = (
    SELECT MIN(u2.id)
    FROM User u2
)
""")
    PlatformStatsProjection getPlatformStats();
    @Query("""
SELECT new com.jobportal.dto.RecruiterAnalyticsProjection(
    COUNT(DISTINCT j.id),
    COUNT(DISTINCT CASE WHEN j.status='OPEN' THEN j.id END),
    COUNT(a.id),
    COUNT(CASE WHEN a.status='SHORTLISTED' THEN 1 END),
    COUNT(CASE WHEN a.status='INTERVIEW' THEN 1 END)
)
FROM Job j
LEFT JOIN Application a ON a.job.id = j.id
WHERE j.recruiter.id = :recruiterId
""")
    RecruiterAnalyticsProjection getRecruiterAnalytics(
            @Param("recruiterId") Long recruiterId
    );


    // ✅ OPTIMIZATION: Eagerly fetches Job and Company in 1 single count + data page query without needing to load User entity first
    @Query(value = "SELECT a FROM Application a " +
            "LEFT JOIN FETCH a.job j " +
            "LEFT JOIN FETCH j.company c " +
            "WHERE a.candidate.email = :email",
            countQuery = "SELECT COUNT(a) FROM Application a WHERE a.candidate.email = :email")
    Page<Application> findByCandidateEmailWithJobAndCompany(@Param("email") String email, Pageable pageable);

    // ✅ OPTIMIZATION: Fixes N+1 problem for collections by batch-initializing skills for the current page records at once
    @Query("SELECT DISTINCT a FROM Application a LEFT JOIN FETCH a.skills WHERE a IN :applications")
    List<Application> initializeSkillsForPage(@Param("applications") List<Application> applications);

    // ✅ OPTIMIZATION: Single-record lookup fetching all related associations at once
    @Query("SELECT a FROM Application a " +
            "LEFT JOIN FETCH a.job j " +
            "LEFT JOIN FETCH j.company c " +
            "LEFT JOIN FETCH a.candidate u " +
            "WHERE a.publicId = :publicId")
    Optional<Application> findByPublicIdWithDetails(@Param("publicId") String publicId);

    // Keep your existing non-conflicting methods below...
    Optional<Application> findByPublicId(String publicId);
    Page<Application> findByCandidate(com.jobportal.entity.User user, Pageable pageable);
}