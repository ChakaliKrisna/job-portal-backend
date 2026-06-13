package com.jobportal.service;

//package com.jobportal.specification;

//package com.jobportal.specification;

import com.jobportal.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> hasJobType(JobType jobType) {
        return (root, query, cb) -> {
            if (jobType == null) return cb.conjunction();
            return cb.equal(root.get("jobType"), jobType);
        };
    }

    public static Specification<Job> hasWorkMode(WorkMode workMode) {
        return (root, query, cb) -> {
            if (workMode == null) return cb.conjunction();
            return cb.equal(root.get("workMode"), workMode);
        };
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) -> {
            if (location == null || location.isEmpty()) {
                return cb.conjunction();
            }
//            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }

    public static Specification<Job> searchKeyword(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.toLowerCase().trim() + "%";

            List<Predicate> predicates = new ArrayList<>();

            // ✅ String fields
            predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            predicates.add(cb.like(cb.lower(root.get("description")), pattern));
            predicates.add(cb.like(cb.lower(root.get("location")), pattern));

            // ✅ FIXED: company (entity → join)
            Join<Job, Company> companyJoin = root.join("company", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(companyJoin.get("name")), pattern));

            // ✅ skills
            Join<Job, String> skillsJoin = root.join("skillsRequired", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(skillsJoin.as(String.class)), pattern));

            query.distinct(true);

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
    public static Specification<Job> belongsToRecruiter(Long recruiterId) {
        return (root, query, cb) -> {
            if (recruiterId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("recruiter").get("id"), recruiterId);
        };
    }

    //    public static Specification<Job> hasExperienceLevel(String experienceLevel) {
//        return (root, query, cb) -> {
//            if (experienceLevel == null || experienceLevel.isEmpty()) {
//                return cb.conjunction();
//            }
//            return cb.like(cb.lower(root.get("experienceLevel")), "%" + experienceLevel.toLowerCase() + "%");
//        };
//    }
    public static Specification<Job> hasMinSalary(Double minSalary) {
        return (root, query, cb) -> {
            if (minSalary == null) {
                return null;
            }

            return cb.greaterThanOrEqualTo(
                    root.get("salary"),
                    minSalary * 100000
            );
        };

    }

    public static Specification<Job> hasExperienceLevel(ExperienceLevel exp) {
        return (root, query, cb) -> {
            if (exp == null) return cb.conjunction();
            return cb.equal(root.get("experienceLevel"), exp);
        };
    }

    public static Specification<Job> hasStatus(JobStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction(); // no filter
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Job> hasCategory(JobCategory category) {
        return (root, query, cb) -> {
            if (category == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category"), category);
        };
    }

//    public static Specification<Job> hasStatus(String status) {
//        return (root, query, cb) -> {
//            if (status == null || status.isEmpty()) return cb.conjunction();
//            return cb.equal(root.get("status"), status);
//        };
//    }

//    public static Specification<Job> hasMinSalary(Double minSalary) {
//        return (root, query, cb) -> {
//            if (minSalary == null || minSalary <= 0) return cb.conjunction();
//            return cb.greaterThanOrEqualTo(root.get("salary"), minSalary);
//        };
//    }

}

