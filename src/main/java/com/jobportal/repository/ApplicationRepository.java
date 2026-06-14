package com.jobportal.repository;

import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidate(User candidate);

    List<Application> findByJob(Job job);

    Optional<Application> findByJobAndCandidate(Job job, User candidate);
    Optional<Application> findByPublicId(String publicId);

    boolean existsByJobAndCandidate(Job job, User candidate);

    Page<Application> findByCandidate(User user, Pageable pageable);

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
}