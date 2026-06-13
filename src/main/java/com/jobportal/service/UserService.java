package com.jobportal.service;

import com.jobportal.dto.RecruiterProfileRequest;

//package com.jobportal.service;

import com.jobportal.dto.StudentProfileRequest;
import com.jobportal.dto.StudentProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    // ================= RECRUITER =================
    void updateRecruiterProfile(String email, RecruiterProfileRequest request);

    int getProfileCompletionPercentage(String email);

//    boolean isProfileCompleted(String email);

    // ================= STUDENT =================
//    void updateStudentProfile(String email, StudentProfileRequest request);

//    int getStudentProfileCompletion(String email);

    Integer getRecruiterProfileCompletion(String name);

    Integer getStudentProfileCompletion(String name);

    StudentProfileResponse getStudentProfile(String name);

    String uploadStudentFiles(String name, MultipartFile profileImage, MultipartFile resume);

    void updateStudentProfile(String name, StudentProfileRequest request);

    StudentProfileResponse getStudentProfileByPublicId(String publicId);
}