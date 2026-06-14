package com.jobportal.repository;

import com.jobportal.dto.JobResponseDTO;
import com.jobportal.entity.Company;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobStatus;
import com.jobportal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
    Page<Job> findAll(
            Specification<Job> spec,
            Pageable pageable
    );

    Page<JobResponseDTO> findRecruiterJobs(User recruiter, Pageable pageable);
}