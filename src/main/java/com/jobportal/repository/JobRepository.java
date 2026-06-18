package com.jobportal.repository;

import com.jobportal.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Optional<Job> findByPublicId(String publicId);

    Optional<Job> findByTitleAndCompanyAndLocation(String title, Company company, String location);

    @Query("""
            SELECT j FROM Job j
            WHERE j.recruiter.id = :recruiterId
            AND LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Job> findByRecruiterAndKeyword(Long recruiterId, String keyword, Pageable pageable);

    Page<Job> findByRecruiter(User recruiter, Pageable pageable);

    Page<Job> findByCompany_PublicId(String publicId, Pageable pageable);

    long countByRecruiter_Id(Long recruiterId);

    long countByRecruiter_IdAndStatus(Long recruiterId, JobStatus jobStatus);

    long countByStatus(JobStatus jobStatus);

    // ⭐ OPTIMIZATION 1: Use EntityGraph to eagerly pull company, recruiter, AND skills out in ONE database query.
    // This stops the application from running N+1 queries when mapping inside convertToCardDTO.
    @EntityGraph(attributePaths = {"company", "recruiter", "skillsRequired"})
    @Query(value = """
    SELECT j FROM Job j
    WHERE (:status IS NULL OR j.status = :status)
    AND (:jobType IS NULL OR j.jobType = :jobType)
    AND (:workMode IS NULL OR j.workMode = :workMode)
    AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
    AND (:category IS NULL OR j.category = :category)
    AND (:minSalary IS NULL OR j.salary >= :minSalary)
    AND (:location IS NULL OR LOWER(j.location) = LOWER(:location))
    AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
""",
            countQuery = """
    SELECT COUNT(j) FROM Job j
    WHERE (:status IS NULL OR j.status = :status)
    AND (:jobType IS NULL OR j.jobType = :jobType)
    AND (:workMode IS NULL OR j.workMode = :workMode)
    AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
    AND (:category IS NULL OR j.category = :category)
    AND (:minSalary IS NULL OR j.salary >= :minSalary)
    AND (:location IS NULL OR LOWER(j.location) = LOWER(:location))
    AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<Job> findFilteredJobs(
            @Param("status") JobStatus status,
            @Param("jobType") JobType jobType,
            @Param("workMode") WorkMode workMode,
            @Param("experienceLevel") ExperienceLevel experienceLevel,
            @Param("category") JobCategory category,
            @Param("minSalary") Double minSalary,
            @Param("location") String location,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"company", "recruiter", "skillsRequired"})
    @Query("""
        SELECT j FROM Job j
        WHERE j.recruiter.id = :recruiterId
        AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:location IS NULL OR LOWER(j.location) = LOWER(:location))
        AND (:jobType IS NULL OR j.jobType = :jobType)
        AND (:workMode IS NULL OR j.workMode = :workMode)
        AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
        AND (:status IS NULL OR j.status = :status)
        AND (:category IS NULL OR j.category = :category)
        AND (:minSalary IS NULL OR j.salary >= :minSalary)
    """)
    List<Job> findMyJobs(
            @Param("recruiterId") Long recruiterId,
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            @Param("workMode") WorkMode workMode,
            @Param("experienceLevel") ExperienceLevel experienceLevel,
            @Param("status") JobStatus status,
            @Param("category") JobCategory category,
            @Param("minSalary") Double minSalary,
            Pageable pageable
    );
}