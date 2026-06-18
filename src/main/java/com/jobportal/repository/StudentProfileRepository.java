package com.jobportal.repository;

import com.jobportal.entity.StudentProfile;
import com.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser_Email(String email);

    Optional<StudentProfile> findByUser(User user);

    // ✅ Background worker updates just this column without loading the entire profile object
    @Modifying
    @Transactional
    @Query("UPDATE StudentProfile s SET s.resumeText = :text WHERE s.id = :id")
    void updateResumeText(@Param("id") Long id, @Param("text") String text);
}