package com.jobportal.service;

import com.jobportal.dto.*;
import com.jobportal.entity.*;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//import com.jobportal.service.UserServiceImpl

//package com.jobportal.service;

import com.jobportal.repository.StudentProfileRepository;
import com.jobportal.repository.RecruiterProfileRepository;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//package com.jobportal.service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;
    @Autowired
    private ResumeExtractorService resumeExtractorService;
    private final Tika tika = new Tika();
    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    // ================= RECRUITER PROFILE =================
    @Override
    public void updateRecruiterProfile(String email, RecruiterProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ROLE_RECRUITER) {
            throw new RuntimeException("Only recruiters allowed");
        }

        RecruiterProfile profile = user.getRecruiterProfile();

        if (profile == null) {
            profile = new RecruiterProfile();
            profile.setUser(user);
        }

        profile.setJobTitle(request.getJobTitle());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setLinkedInUrl(request.getLinkedInUrl());
        profile.setBio(request.getDescription());

        recruiterProfileRepository.save(profile);
        user.setRecruiterProfile(profile);

        Company company = user.getCompany();

        if (company == null) {
            company = new Company();
        }

        company.setName(request.getCompanyName());
        company.setWebsite(request.getWebsite());
        company.setDescription(request.getDescription());
        company.setLocation(request.getLocation());
        company.setIndustry(request.getIndustry());
        company.setCompanySize(request.getCompanySize());
//        company.setFoundedYear(Integer.valueOf(request.getFoundedYear()));
        company.setLogoUrl(request.getLogoUrl());

        companyRepository.save(company);

        user.setCompany(company);
        userRepository.save(user);
    }

    // ================= RECRUITER COMPLETION =================
    @Override
    public int getProfileCompletionPercentage(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ROLE_RECRUITER) return 100;

        Company company = user.getCompany();
        if (company == null) return 0;

        int score = 0;

        if (hasText(company.getName())) score += 30;
        if (hasText(company.getWebsite())) score += 15;
        if (hasText(company.getDescription())) score += 15;
        if (hasText(company.getLocation())) score += 10;
        if (hasText(company.getIndustry())) score += 10;
        if (hasText(company.getCompanySize())) score += 10;
        if (hasText(company.getLogoUrl())) score += 10;

        return score;
    }
    // ================= STUDENT PROFILE =================
    @Override
//    @Override
    public void updateStudentProfile(String email, StudentProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ROLE_STUDENT) {
            throw new RuntimeException("Only students allowed");
        }

        StudentProfile profile = user.getStudentProfile();

        if (profile == null) {
            profile = new StudentProfile();
            profile.setUser(user);
        }

        // ===== BASIC FIELDS =====
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setLocation(request.getLocation());
        profile.setHeadline(request.getHeadline());
        profile.setEducation(request.getEducation());
        profile.setExperience(request.getExperience());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setAchievements(request.getAchievements());
//        profile.setUser(user);

        // ===== SKILLS =====
        if (profile.getSkills() == null) {
            profile.setSkills(new java.util.ArrayList<>());
        } else {
            profile.getSkills().clear();
        }

        if (request.getSkills() != null) {

            final StudentProfile finalProfile = profile;

            List<StudentSkill> skills = request.getSkills()
                    .stream()
                    .map(skill -> {
                        StudentSkill ss = new StudentSkill();
                        ss.setSkill(skill);
                        ss.setProfile(finalProfile);
                        return ss;
                    })
                    .toList();

            profile.getSkills().addAll(skills);
        }
        // ===== PROJECTS =====
        if (profile.getProjects() == null) {
            profile.setProjects(new java.util.ArrayList<>());
        } else {
            profile.getProjects().clear();
        }

        if (request.getProjects() != null) {
            for (ProjectRequest req : request.getProjects()) {

                Project p = new Project();
                p.setTitle(req.getTitle());
                p.setDescription(req.getDescription());
                p.setTechStack(req.getTechStack());
                p.setGithubLink(req.getGithubLink());

                p.setProfile(profile); // VERY IMPORTANT

                profile.getProjects().add(p);
            }
        }

        studentProfileRepository.save(profile);

        user.setStudentProfile(profile);
        userRepository.save(user);
    }// ================= STUDENT COMPLETION =================
    @Override
    public Integer getStudentProfileCompletion(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ROLE_STUDENT) return 100;

        StudentProfile profile = user.getStudentProfile();
        if (profile == null) return 0;

        int score = 0;

        // ===== BASIC INFO (20) =====
        if (hasText(profile.getPhoneNumber())) score += 5;
        if (hasText(profile.getLocation())) score += 5;
        if (hasText(profile.getHeadline())) score += 10;

        // ===== ACADEMIC (15) =====
        if (hasText(profile.getEducation())) score += 10;
        if (hasText(profile.getExperience())) score += 5;

        // ===== SKILLS (15) =====
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            score += 15;
        }

        // ===== PROJECTS (15) =====
        if (profile.getProjects() != null && !profile.getProjects().isEmpty()) {
            score += 15;
        }

        // ===== RESUME (15) =====
        if (hasText(profile.getResumeUrl())) score += 15;

        // ===== LINKS (10) =====
        if (hasText(profile.getGithubUrl())) score += 5;
        if (hasText(profile.getLinkedinUrl())) score += 5;

        // ===== EXTRA (10) =====
        if (hasText(profile.getAchievements())) score += 5;
        if (hasText(profile.getProfileImageUrl())) score += 5;

        return score; // total = 100
    }    @Override
    public StudentProfileResponse getStudentProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudentProfile profile = user.getStudentProfile();

        StudentProfileResponse response = new StudentProfileResponse();
        response.setPublicId(user.getPublicId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        response.setPhoneNumber(profile.getPhoneNumber());
        response.setLocation(profile.getLocation());
        response.setHeadline(profile.getHeadline());

        response.setSkills(
                profile.getSkills()
                        .stream()
                        .map(StudentSkill::getSkill)
                        .toList()
        );
        response.setEducation(profile.getEducation());
        response.setExperience(profile.getExperience());

        response.setResumeUrl(profile.getResumeUrl());

        response.setGithubUrl(profile.getGithubUrl());
        response.setLinkedinUrl(profile.getLinkedinUrl());

        // ✅ ADD THIS (you were missing)
        response.setAchievements(profile.getAchievements());

        // ✅ PROJECTS MAPPING
        List<ProjectResponse> projects = profile.getProjects() != null
                ? profile.getProjects().stream()
                .map(p -> new ProjectResponse(
                        p.getTitle(),
                        p.getDescription(),
                        p.getTechStack(),
                        p.getGithubLink()
                ))
                .collect(Collectors.toList())
                : new ArrayList<>();

        response.setProjects(projects);

        return response;
    }

    @Override
    public Integer getRecruiterProfileCompletion(String name) {
        return 0;
    }

    // ================= UTILITY =================
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
//    @Autowired
    private final com.jobportal.service.ResumeAsyncTaskService resumeAsyncTaskService; // Inject this helper service
//    private final UserRepository userRepository;
//    private final StudentProfileRepository studentProfileRepository;

    @Override
    public String uploadStudentFiles(
            String email,
            MultipartFile profileImage,
            MultipartFile resume
    ) {

        try {

            String basePath = System.getProperty("user.dir") + "/uploads/";

            Path profileDir = Paths.get(basePath, "profile-images");
            Path resumeDir = Paths.get(basePath, "resumes");

            Files.createDirectories(profileDir);
            Files.createDirectories(resumeDir);

            StudentProfile profile = studentProfileRepository
                    .findByUser_Email(email)
                    .orElseGet(() -> {

                        User user = userRepository.findByEmail(email)
                                .orElseThrow(() ->
                                        new RuntimeException("User not found"));

                        StudentProfile newProfile = new StudentProfile();
                        newProfile.setUser(user);

                        return newProfile;
                    });

            long timestamp = System.currentTimeMillis();

            // ================= PROFILE IMAGE =================

            if (profileImage != null && !profileImage.isEmpty()) {

                String imageName = timestamp + "_"
                        + profileImage.getOriginalFilename()
                        .replaceAll("[^a-zA-Z0-9.-]", "_");

                Path imagePath = profileDir.resolve(imageName);

                profileImage.transferTo(imagePath.toFile());


                profile.setProfileImageUrl(
                        "/uploads/profile-images/" + imageName
                );
            }

            // ================= RESUME =================

            if (resume != null && !resume.isEmpty()) {

                String resumeName = timestamp + "_"
                        + resume.getOriginalFilename()
                        .replaceAll("[^a-zA-Z0-9.-]", "_");

                Path resumePath =
                        resumeDir.resolve(resumeName);

                resume.transferTo(resumePath.toFile());

                String extractedText = "";

                try (PDDocument document =
                             Loader.loadPDF(resumePath.toFile())) {

                    PDFTextStripper stripper =
                            new PDFTextStripper();

                    extractedText =
                            stripper.getText(document);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                profile.setResumeText(extractedText);

                profile.setResumeUrl(
                        "/uploads/resumes/" + resumeName
                );

                profile.setResumeFileName(
                        resume.getOriginalFilename()
                );
            }

            profile = studentProfileRepository.save(profile);

            return profile.getResumeUrl();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "File upload processing faulted",
                    e
            );
        }
    }



    public StudentProfileResponse getStudentProfileByPublicId(String publicId) {

        User student = userRepository.findByPublicId(publicId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found"));

        StudentProfile profile = student.getStudentProfile();

        StudentProfileResponse dto =
                new StudentProfileResponse();
        List<ProjectResponse> projects = profile.getProjects() != null
                ? profile.getProjects().stream()
                .map(p -> new ProjectResponse(
                        p.getTitle(),
                        p.getDescription(),
                        p.getTechStack(),
                        p.getGithubLink()
                ))
                .collect(Collectors.toList())
                : new ArrayList<>();

        dto.setName(student.getName());
        dto.setPublicId(student.getPublicId());
        System.out.println(student.getPublicId()+ "############");

        dto.setEmail(student.getEmail());

        dto.setSkills(
                Optional.ofNullable(profile.getSkills())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(StudentSkill::getSkill)
                        .toList()
        );

        dto.setEducation(profile.getEducation());

//        dto(profile.getCollege());
        dto.setPublicId(student.getPublicId());

        dto.setProfileImageUrl(
                profile.getProfileImageUrl()
        );
        dto.setGithubUrl(profile.getGithubUrl());

        dto.setLinkedinUrl(profile.getLinkedinUrl());

        dto.setResumeUrl(profile.getResumeUrl());

        dto.setProjects(projects);

        return dto;
    }
}