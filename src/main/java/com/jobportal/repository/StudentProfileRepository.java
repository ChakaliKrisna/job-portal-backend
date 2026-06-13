package com.jobportal.repository;

//package com.jobportal.repository;

import com.jobportal.entity.StudentProfile;
import com.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//import java.lang.ScopedValue;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser_Email(String email);

//    ScopedValue<StudentProfile> findByUser(User user);
    Optional<StudentProfile> findByUser(User user);
}