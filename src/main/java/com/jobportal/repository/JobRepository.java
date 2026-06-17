package com.jobportal.repository;

import com.jobportal.dto.JobResponseDTO;
import com.jobportal.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository
        extends JpaRepository<Job, Long>,
        JpaSpecificationExecutor<Job> {

    Optional<Job> findByPublicId(String publicId);

    Optional<Job> findByTitleAndCompanyAndLocation(
            String title,
            Company company,
            String location
    );

    @Query("""
            SELECT j
            FROM Job j
            WHERE j.recruiter.id = :recruiterId
            AND LOWER(j.title)
            LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Job> findByRecruiterAndKeyword(
            Long recruiterId,
            String keyword,
            Pageable pageable
    );

    Page<Job> findByRecruiter(
            User recruiter,
            Pageable pageable
    );

    Page<Job> findByCompany_PublicId(
            String publicId,
            Pageable pageable
    );

    long countByRecruiter_Id(Long recruiterId);

    long countByRecruiter_IdAndStatus(
            Long recruiterId,
            JobStatus jobStatus
    );

    long countByStatus(JobStatus jobStatus);

    @EntityGraph(attributePaths = {
            "company",
            "recruiter"
    })
    @Query("""
    SELECT j FROM Job j
    JOIN FETCH j.company c
    WHERE (:status IS NULL OR j.status = :status)
    AND (:jobType IS NULL OR j.jobType = :jobType)
    AND (:workMode IS NULL OR j.workMode = :workMode)
    AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
    AND (:category IS NULL OR j.category = :category)
    AND (:minSalary IS NULL OR j.salary >= :minSalary)
    AND (:location IS NULL OR j.location = :location)
    AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY j.postedDate DESC
""")
    Page<Job> findFilteredJobs(
            JobStatus status,
            JobType jobType,
            WorkMode workMode,
            ExperienceLevel experienceLevel,
            JobCategory category,
            Double minSalary,
            String location,
            String keyword,
            Pageable pageable
    );

    @Query("""
        SELECT j FROM Job j
        LEFT JOIN FETCH j.company c
        LEFT JOIN FETCH j.recruiter r
        WHERE j.recruiter.id = :recruiterId
        AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:location IS NULL OR j.location = :location)
        AND (:jobType IS NULL OR j.jobType = :jobType)
        AND (:workMode IS NULL OR j.workMode = :workMode)
        AND (:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel)
        AND (:status IS NULL OR j.status = :status)
        AND (:category IS NULL OR j.category = :category)
        AND (:minSalary IS NULL OR j.salary >= :minSalary)
        ORDER BY j.postedDate DESC
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

//    Page<JobResponseDTO> findRecruiterJobs(User recruiter, Pageable pageable);
}