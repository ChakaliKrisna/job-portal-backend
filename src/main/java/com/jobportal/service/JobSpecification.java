package com.jobportal.service;

//package com.jobportal.specification;

//package com.jobportal.specification;

import com.jobportal.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> keyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            return cb.like(
                    cb.lower(root.get("title")),
                    "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Job> location(String location) {
        return (root, query, cb) ->
                (location == null || location.isBlank())
                        ? cb.conjunction()
                        : cb.equal(root.get("location"), location);
    }

    public static Specification<Job> jobType(JobType jobType) {
        return (root, query, cb) ->
                jobType == null ? cb.conjunction()
                        : cb.equal(root.get("jobType"), jobType);
    }

    public static Specification<Job> workMode(WorkMode workMode) {
        return (root, query, cb) ->
                workMode == null ? cb.conjunction()
                        : cb.equal(root.get("workMode"), workMode);
    }

    public static Specification<Job> status(JobStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Job> category(JobCategory category) {
        return (root, query, cb) ->
                category == null ? cb.conjunction()
                        : cb.equal(root.get("category"), category);
    }

    public static Specification<Job> minSalary(Double minSalary) {
        return (root, query, cb) ->
                minSalary == null ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get("salary"), minSalary);
    }
    public static Specification<Job> recruiter(Long recruiterId) {

        return (root, query, cb) ->
                recruiterId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("recruiter").get("id"), recruiterId);
    }
    public static Specification<Job> experienceLevel(
            ExperienceLevel experienceLevel) {

        return (root, query, cb) ->
                experienceLevel == null
                        ? cb.conjunction()
                        : cb.equal(root.get("experienceLevel"), experienceLevel);
    }
}