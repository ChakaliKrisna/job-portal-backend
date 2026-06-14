package com.jobportal.service;

import com.jobportal.controller.ApplicationDetailsDto;
import com.jobportal.dto.*;
import com.jobportal.entity.Application;
import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.repository.ApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

//package com.jobportal.service;

import com.jobportal.entity.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.jobportal.entity.ApplicationStatus.REVIEWED;
import static com.jobportal.entity.NotificationType.INTERVIEWED;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);


    @Autowired
    private ApplicationRepository applicationRepo;

    @Autowired
    private JobRepository jobRepo;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepo;

    // ================= APPLY JOB =================
    @Transactional
    @Override
    public void apply(String jobPublicId, ApplyRequest request) {

        // ================= Logged-in Candidate =================
        User candidate = getLoggedInUser();

        // ================= Fetch Job =================
        Job job = jobRepo.findByPublicId(jobPublicId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ================= Prevent Duplicate =================
        if (applicationRepo.existsByJobAndCandidate(job, candidate)) {
            throw new RuntimeException("Already applied for this job");
        }

        // ================= Student Profile =================
        StudentProfile profile = candidate.getStudentProfile();

        if (profile == null) {
            throw new RuntimeException("Student profile not found");
        }

        // ================= Create Application =================
        Application app = new Application();

        app.setCandidate(candidate);
        app.setJob(job);

        // ================= Candidate Identity =================
        if (Boolean.TRUE.equals(request.getOverride())) {

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new RuntimeException("Custom name is required");
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Custom email is required");
            }

            app.setCandidateName(request.getName().trim());
            app.setCandidateEmail(request.getEmail().trim());

        } else {
            app.setCandidateName(candidate.getName());
            app.setCandidateEmail(candidate.getEmail());
        }

        // ================= Resume =================
        String resume = (request.getResumeUrl() != null && !request.getResumeUrl().trim().isEmpty())
                ? request.getResumeUrl().trim()
                : profile.getResumeUrl();

        if (resume == null || resume.trim().isEmpty()) {
            throw new RuntimeException("Resume is required");
        }

        app.setResumeUrl(resume);
        app.setResumeText(profile.getResumeText());

        // ================= Cover Letter =================
        String coverLetter = request.getCoverLetter();

        if (coverLetter == null || coverLetter.trim().length() < 20) {
            throw new RuntimeException("Cover letter must be at least 20 characters");
        }

        if (coverLetter.length() > 1000) {
            throw new RuntimeException("Cover letter exceeds 1000 characters");
        }

        app.setCoverLetter(coverLetter.trim());

        // ================= Extra Skills =================
        List<String> extraSkills = Optional.ofNullable(request.getExtraSkills())
                .orElse(new ArrayList<>())
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        // ================= Core Skills =================
        List<String> userSkills =
                Optional.ofNullable(profile.getSkills())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(StudentSkill::getSkill)
                        .toList();

        Set<String> allSkills = new LinkedHashSet<>();

        userSkills.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(allSkills::add);

        allSkills.addAll(extraSkills);

        // ================= CONVERT TO ENTITY (IMPORTANT FIX) =================
        List<ApplicationSkill> skillEntities = allSkills.stream()
                .map(skill -> {
                    ApplicationSkill s = new ApplicationSkill();
                    s.setSkill(skill);
                    s.setApplication(app);
                    return s;
                })
                .toList();

        app.setSkills(skillEntities);

        // ================= Match Score =================
        List<String> jobSkills =
                job.getSkillsRequired()
                        .stream()
                        .map(JobSkill::getSkill)
                        .toList();

        double score = calculateScore(
                userSkills,
                extraSkills,
                jobSkills,
                profile.getResumeText(),
                job.getDescription()
        );

        app.setMatchScore(score);

        // ================= Optional Fields =================
        app.setAvailability(request.getAvailability());
        app.setWorkPreference(request.getWorkPreference());

        // ================= Save Notification =================
        notificationService.createNotification(
                candidate,
                "Application Submitted",
                "You successfully applied for " + job.getTitle(),
                NotificationType.JOB_APPLIED
        );

        // ================= Save Application =================
        applicationRepo.save(app);
    }


    @Override
    public Page<ApplicationResponse> getMyApplications(int page, int size) {

        // =========================================================
        // ✅ Logged-in User
        // =========================================================

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // =========================================================
        // ✅ Pagination + Sorting
        // =========================================================

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("appliedAt").descending()
        );

        // =========================================================
        // ✅ Fetch Applications
        // =========================================================

        Page<Application> applications =
                applicationRepo.findByCandidate(user, pageable);

        // =========================================================
        // ✅ Entity → DTO Mapping
        // =========================================================

        return applications.map(app -> {

            Job job = app.getJob();

            String companyName =
                    (job != null && job.getCompany() != null)
                            ? job.getCompany().getName()
                            : "N/A";

            return ApplicationResponse.builder()

                    // IDs
                    .applicationId(app.getPublicId())

                    .jobId(
                            job != null
                                    ? job.getPublicId()
                                    : null
                    )

                    // Job Info
                    .jobTitle(
                            job != null
                                    ? job.getTitle()
                                    : "Unknown Job"
                    )

                    .companyName(companyName)

                    // Candidate Info
                    .candidateName(app.getCandidateName())

                    .candidateEmail(app.getCandidateEmail())

                    // Status
                    .status(
                            app.getStatus() != null
                                    ? app.getStatus().name()
                                    : "PENDING"
                    )

                    // Match Score
                    .matchScore(
                            app.getMatchScore() != null
                                    ? app.getMatchScore()
                                    : 0.0
                    )

                    // Skills
                    .skills(
                            app.getSkills() != null
                                    ? app.getSkills()
                                    .stream()
                                    .map(ApplicationSkill::getSkill)
                                    .toList()
                                    : Collections.emptyList()
                    )

                    // Resume & Cover Letter
                    .resumeUrl(app.getResumeUrl())

                    .coverLetter(app.getCoverLetter())

                    // Time
                    .appliedAt(app.getAppliedAt())

                    .build();
        });
    }

    private User getLoggedInUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (auth == null) throw new RuntimeException("Unauthorized");

        String email = auth.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ================= MY APPLICATIONS (PAGINATION) =================

//        app.setMatchScore(score);

    private double calculateScore(
            List<String> userSkills,
            List<String> extraSkills,
            List<String> jobSkills,
            String resumeText,
            String jobDescription
    ) {

        // =========================================================
        // ✅ Safe Lists
        // =========================================================

        userSkills = Optional.ofNullable(userSkills)
                .orElse(new ArrayList<>());

        extraSkills = Optional.ofNullable(extraSkills)
                .orElse(new ArrayList<>());

        jobSkills = Optional.ofNullable(jobSkills)
                .orElse(new ArrayList<>());

        // =========================================================
        // ✅ Combine Skills
        // =========================================================

        Set<String> candidateSkills = new HashSet<>();

// =========================================================
// ✅ Profile Skills
// =========================================================

        userSkills.stream()
                .filter(Objects::nonNull)
                .map(this::simplifySkill)
                .forEach(candidateSkills::add);

// =========================================================
// ✅ Extra Skills
// =========================================================

        extraSkills.stream()
                .filter(Objects::nonNull)
                .map(this::simplifySkill)
                .forEach(candidateSkills::add);

// =========================================================
// ✅ Resume Keyword Skills
// =========================================================

        String normalizedResume =
                normalize(
                        Optional.ofNullable(resumeText)
                                .orElse("")
                );

        jobSkills.stream()
                .filter(Objects::nonNull)
                .map(this::simplifySkill)
                .forEach(skill -> {

                    if (normalizedResume.contains(skill)) {
                        candidateSkills.add(skill);
                    }
                });
        Set<String> requiredSkills = jobSkills.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .collect(Collectors.toSet());

        // =========================================================
        // ✅ SKILL MATCH SCORE (80%)
        // =========================================================

        long matchedSkills = requiredSkills.stream()
                .filter(candidateSkills::contains)
                .count();

        double skillScore = requiredSkills.isEmpty()
                ? 0
                : ((double) matchedSkills / requiredSkills.size()) * 80;

        // =========================================================
        // ✅ RESUME KEYWORD SCORE (20%)
        // =========================================================

        double keywordScore = 0;

        String resume = normalize(
                Optional.ofNullable(resumeText).orElse("")
        );

        String jobDesc = normalize(
                Optional.ofNullable(jobDescription).orElse("")
        );

        for (String skill : requiredSkills) {

            String simplifiedSkill = simplifySkill(skill);

            boolean inResume =
                    resume.contains(simplifiedSkill);

            boolean inJobDesc =
                    jobDesc.contains(simplifiedSkill);

            if (inResume) {
                keywordScore += 3;
            }

            if (inResume && inJobDesc) {
                keywordScore += 2;
            }
        }

        // Max 20%
        keywordScore = Math.min(keywordScore, 20);

        // =========================================================
        // ✅ Bonus For Extra Skills
        // =========================================================

        double extraSkillBonus = 0;

        if (extraSkills.size() >= 3) {
            extraSkillBonus = 5;
        }

        // =========================================================
        // ✅ Final ATS Score
        // =========================================================

        double finalScore =
                skillScore
                        + keywordScore
                        + extraSkillBonus;

        return Math.min(finalScore, 100);
    }

    private String normalize(String text) {

        return text.toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    // ================= JOB APPLICATIONS (PAGINATION) =================
    @Override
    public Page<ApplicationCandidateResponse> getApplicationsByJob(
            String jobPublicId, int page, int size) {

        Job job = jobRepo.findByPublicId(jobPublicId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("appliedAt").descending()
        );

        Page<Application> applications =
                applicationRepo.findByJob(job, pageable);

        return applications.map(this::mapToCandidateResponse);
    }

    @Override
    public Double calculateMatchScore(
            String jobId,
            List<String> extraSkills
    ) {

        // =========================================================
        // ✅ Logged-in User
        // =========================================================

        User candidate = getLoggedInUser();

        // =========================================================
        // ✅ Fetch Job
        // =========================================================

        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // =========================================================
        // ✅ Student Profile
        // =========================================================

        StudentProfile profile = candidate.getStudentProfile();

        // =========================================================
        // ✅ User Skills
        // =========================================================

        List<String> userSkills =
                Optional.ofNullable(profile.getSkills())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(StudentSkill::getSkill)
                        .toList();
        // =========================================================
        // ✅ Job Skills
        // =========================================================

        List<String> jobSkills =
                Optional.ofNullable(job.getSkillsRequired())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(JobSkill::getSkill)
                        .toList();

        // =========================================================
        // ✅ Resume Text
        // =========================================================

        String resumeText =
                profile.getResumeText() != null
                        ? profile.getResumeText()
                        : "";

        // =========================================================
        // ✅ Job Description
        // =========================================================

        String jobDescription =
                job.getDescription() != null
                        ? job.getDescription()
                        : "";

        // =========================================================
        // ✅ Calculate Score
        // =========================================================

        return calculateScore(
                userSkills,
                extraSkills,
                jobSkills,
                resumeText,
                jobDescription
        );
    }

    @Override
    public List<String> getMissingSkillsForApplicationBeforeApply(String jobId) {
        User user = getLoggedInUser();

        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        StudentProfile profile = user.getStudentProfile();

        // ✅ Safe user skills
        List<String> userSkills =
                Optional.ofNullable(profile)
                        .map(StudentProfile::getSkills)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(StudentSkill::getSkill)
                        .toList();

        List<String> jobSkills =
                Optional.ofNullable(job.getSkillsRequired())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(JobSkill::getSkill)
                        .toList();

        // ✅ Normalize user skills
        Set<String> userSet = userSkills.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toSet());

        // ✅ Find missing skills
        return jobSkills.stream()
                .filter(Objects::nonNull)
                .filter(skill -> !userSet.contains(skill.toLowerCase().trim()))
                .collect(Collectors.toList());
    }


    // ================= FIND BY PUBLIC ID =================
    @Override
    public Optional<Application> findByPublicId(String publicId) {
        return applicationRepo.findByPublicId(publicId);
    }

    // ================= UPDATE STATUS (RECRUITER ONLY) =================
    @Override
    public void updateStatus(String publicId, ApplicationStatus status) {

        Application app = applicationRepo.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        try {
            app.setStatus(status);
        } catch (Exception e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        applicationRepo.save(app);
        String message = switch (status) {

            case REVIEWED -> "Your application for " +
                    app.getJob().getTitle() +
                    " has been reviewed by the recruiter.";

            case SHORTLISTED -> "Congratulations! You have been shortlisted for " +
                    app.getJob().getTitle() + ".";

            case INTERVIEW -> "Your application for " +
                    app.getJob().getTitle() +
                    " moved to the interview stage.";

            case HIRED -> "Congratulations! You have been hired for " +
                    app.getJob().getTitle() + ".";

            case REJECTED -> "Your application for " +
                    app.getJob().getTitle() +
                    " was not selected this time.";

            default -> "Your application status was updated.";
        };

        notificationService.createNotification(
                app.getCandidate(),
                "Application Status Updated",
                message,
                NotificationType.STATUS_UPDATED
        );


        logger.info("Application {} status updated to {}", publicId, status);

    }


    @Override
    public boolean isAlreadyApplied(String jobId) {

        // ✅ Get logged-in user
        User candidate = getLoggedInUser();

        // ✅ Get job
        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ✅ Check in DB
        return applicationRepo.existsByJobAndCandidate(job, candidate);
    }

    @Override
    public ApplicationResponse getApplicationById(String applicationId) {

        User user = getLoggedInUser();

        Application app = applicationRepo.findByPublicId(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // 🔐 Security check (VERY IMPORTANT)
        if (!app.getCandidate().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return ApplicationResponse.builder()
                .applicationId(app.getPublicId())
                .jobTitle(app.getJob().getTitle())
                .companyName(
                        app.getJob() != null && app.getJob().getCompany() != null
                                ? app.getJob().getCompany().getName()
                                : "N/A"
                )
                .jobId(app.getJob().getPublicId())
                .status(app.getStatus().name())
                .matchScore(app.getMatchScore())
                .skills(
                        app.getSkills() != null
                                ? app.getSkills()
                                .stream()
                                .map(ApplicationSkill::getSkill)
                                .toList()
                                : Collections.emptyList()
                )
                .resumeUrl(app.getResumeUrl())
                .coverLetter(app.getCoverLetter())
                .appliedAt(app.getAppliedAt())
                .build();
    }


    @Override
    public List<String> getMissingSkillsByApplication(String applicationId) {

        // ✅ 1. Get logged-in use
        User user = getLoggedInUser();

        // ✅ 2. Get application directly
        Application app = applicationRepo.findByPublicId(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // 🔐 3. Security check
        // candidate owner
        boolean isCandidate =
                app.getCandidate().getId().equals(user.getId());

// recruiter owner of the job
        boolean isRecruiter =
                app.getJob().getRecruiter().getId().equals(user.getId());

        if (!isCandidate && !isRecruiter) {
            throw new RuntimeException("Unauthorized access");
        }

        // ✅ 4. Get job from application
        Job job = app.getJob();

        List<String> jobSkills =
                Optional.ofNullable(app.getJob().getSkillsRequired())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(JobSkill::getSkill)
                        .toList();
        List<String> appliedSkills = Optional.ofNullable(app.getSkills())
                .orElse(new ArrayList<>())
                .stream()
                .map(ApplicationSkill::getSkill)
                .toList();

        // ✅ 5. Normalize applied skills
        Set<String> appliedSet = appliedSkills.stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toSet());

        // ✅ 6. Find missing skills
        return jobSkills.stream()
                .filter(Objects::nonNull)
                .filter(skill -> !appliedSet.contains(skill.toLowerCase().trim()))
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationResponse getMyApplication(String jobId) {

        User user = getLoggedInUser();

        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        Application app = applicationRepo
                .findByJobAndCandidate(job, user)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        return ApplicationResponse.builder()
                .applicationId(app.getPublicId())
                .jobTitle(app.getJob().getTitle())
                .companyName(
                        app.getJob() != null && app.getJob().getCompany() != null
                                ? app.getJob().getCompany().getName()
                                : "N/A"
                )
                .jobId(app.getJob().getPublicId())
                .status(app.getStatus().name())
                .matchScore(app.getMatchScore())
                .skills(
                        app.getSkills().stream()
                                .filter(s -> s.getType() == SkillType.SNAPSHOT)
                                .map(ApplicationSkill::getSkill)
                                .toList()
                )
                .extraSkills(
                        app.getSkills().stream()
                                .filter(s -> s.getType() == SkillType.EXTRA)
                                .map(ApplicationSkill::getSkill)
                                .toList()
                )
                .resumeUrl(app.getResumeUrl())
                .coverLetter(app.getCoverLetter())
                .appliedAt(app.getAppliedAt())
                .build();
    }

    public ApplicationDetailsDto getApplicationDetails(String publicId, User student) {

        Application app = applicationRepo.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Security check
        if (!app.getCandidate().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<String> snapshotSkills =
                Optional.ofNullable(app.getSkills())
                        .orElse(List.of())
                        .stream()
                        .filter(s -> s.getType() == SkillType.SNAPSHOT)
                        .map(ApplicationSkill::getSkill)
                        .toList();

        Set<String> snapshotSet =
                snapshotSkills.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet());
        List<String> jobSkills =
                Optional.ofNullable(app.getJob().getSkillsRequired())
                        .orElse(List.of())
                        .stream()
                        .map(JobSkill::getSkill)
                        .toList();

        List<String> missingSkills =
                jobSkills.stream()
                        .filter(skill -> !snapshotSet.contains(skill.toLowerCase()))
                        .toList();
        String companyName = Optional.ofNullable(app.getJob())
                .map(Job::getCompany)
                .map(Company::getName)
                .orElse("N/A");

        return ApplicationDetailsDto.builder()
                .applicationPublicId(app.getPublicId())
                .jobTitle(app.getJob().getTitle())
                .companyName(companyName)

                .status(app.getStatus().name())
                .matchScore(app.getMatchScore())
                .missingSkills(missingSkills)
                .resumeUrl(app.getResumeUrl())
                .coverLetter(app.getCoverLetter())
                .availability(app.getAvailability())
                .workPreference(app.getWorkPreference())
                .extraSkills(
                        app.getSkills().stream()
                                .filter(s -> s.getType() == SkillType.EXTRA)
                                .map(ApplicationSkill::getSkill)
                                .toList()
                )
                .appliedAt(app.getAppliedAt())
                .build();
    }

    @Override
    public Page<ApplicationCandidateResponse> filterCandidatesGlobal(

            String keyword,
            Double minScore,
            String status,
            String skill,
            int page,
            int size
    ) {

        // =========================================================
        // ✅ Fetch ALL Applications (⚠️ consider replacing with DB filtering later)
        // =========================================================
        List<Application> applications = applicationRepo.findAll();

        // =========================================================
        // ✅ Apply Filters
        // =========================================================
        List<ApplicationCandidateResponse> filtered =
                applications.stream()

                        // ================= Resume keyword filter =================
                        .filter(app -> {
                            if (keyword == null || keyword.isBlank()) return true;

                            return app.getResumeText() != null &&
                                    app.getResumeText().toLowerCase()
                                            .contains(keyword.toLowerCase());
                        })

                        // ================= Match score filter =================
                        .filter(app -> {
                            if (minScore == null) return true;

                            return app.getMatchScore() != null &&
                                    app.getMatchScore() >= minScore;
                        })

                        // ================= Status filter =================
                        .filter(app -> {
                            if (status == null || status.isBlank()) return true;

                            return app.getStatus() != null &&
                                    app.getStatus().name().equalsIgnoreCase(status);
                        })

                        // ================= Skill filter (FIXED) =================
                        .filter(app -> {
                            if (skill == null || skill.isBlank()) return true;

                            return app.getSkills() != null &&
                                    app.getSkills().stream()
                                            .anyMatch(s ->
                                                    s.getType() == SkillType.SNAPSHOT &&
                                                            s.getSkill().equalsIgnoreCase(skill)
                                            );
                        })

                        // ================= Sort by score DESC =================
                        .sorted((a, b) -> Double.compare(
                                b.getMatchScore() != null ? b.getMatchScore() : 0,
                                a.getMatchScore() != null ? a.getMatchScore() : 0
                        ))

                        // ================= DTO Mapping =================
                        .map(this::mapToCandidateResponse)
                        .toList();

        // =========================================================
        // ✅ Manual Pagination
        // =========================================================
        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        List<ApplicationCandidateResponse> paginated =
                start >= filtered.size()
                        ? Collections.emptyList()
                        : filtered.subList(start, end);

        return new PageImpl<>(
                paginated,
                PageRequest.of(page, size),
                filtered.size()
        );
    }

    private ApplicationCandidateResponse mapToCandidateResponse(Application app) {

        return ApplicationCandidateResponse.builder()

                .applicationId(app.getPublicId())

                .studentPublicId(
                        app.getCandidate() != null ? app.getCandidate().getPublicId() : null
                )

                .candidateName(
                        app.getCandidateName() != null ? app.getCandidateName() : "N/A"
                )

                .email(
                        app.getCandidateEmail() != null ? app.getCandidateEmail() : "N/A"
                )

                .jobPublicId(
                        app.getJob() != null ? app.getJob().getPublicId() : null
                )

                .jobTitle(
                        app.getJob() != null ? app.getJob().getTitle() : "N/A"
                )

                .companyName(
                        app.getJob() != null && app.getJob().getCompany() != null
                                ? app.getJob().getCompany().getName()
                                : "N/A"
                )

                .status(
                        app.getStatus() != null ? app.getStatus().name() : "APPLIED"
                )

                .matchScore(
                        app.getMatchScore() != null ? app.getMatchScore() : 0.0
                )

                // ================= FIXED SKILLS =================
                .skills(
                        app.getSkills() != null
                                ? app.getSkills().stream()
                                .filter(s -> s.getType() == SkillType.SNAPSHOT)
                                .map(ApplicationSkill::getSkill)
                                .toList()
                                : Collections.emptyList()
                )

                .extraSkills(
                        app.getSkills() != null
                                ? app.getSkills().stream()
                                .filter(s -> s.getType() == SkillType.EXTRA)
                                .map(ApplicationSkill::getSkill)
                                .toList()
                                : Collections.emptyList()
                )

                .resumeUrl(app.getResumeUrl())
                .coverLetter(app.getCoverLetter())

                .viewed(app.getViewed() != null && app.getViewed())
                .appliedAt(app.getAppliedAt())

                .build();
    }

    @Override
    public Page<ApplicationCandidateResponse> filterCandidatesByJob(

            String jobId,
            String keyword,
            Double minScore,
            String status,
            String skill,
            int page,
            int size
    ) {

        // =========================================================
        // Fetch Job
        // =========================================================
        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // =========================================================
        // Fetch Applications
        // =========================================================
        List<Application> applications = applicationRepo.findByJob(job);

        // =========================================================
        // Filter + Map
        // =========================================================
        List<ApplicationCandidateResponse> filtered = applications.stream()

                // keyword (resume)
                .filter(app -> keyword == null || keyword.isBlank()
                        || (app.getResumeText() != null &&
                        app.getResumeText().toLowerCase().contains(keyword.toLowerCase()))
                )

                // match score
                .filter(app -> minScore == null
                        || (app.getMatchScore() != null && app.getMatchScore() >= minScore)
                )

                // status
                .filter(app -> status == null || status.isBlank()
                        || app.getStatus().name().equalsIgnoreCase(status)
                )

                // skill filter (FIXED)
                .filter(app -> {
                    if (skill == null || skill.isBlank()) return true;

                    return app.getSkills() != null &&
                            app.getSkills().stream()
                                    .map(ApplicationSkill::getSkill)
                                    .anyMatch(s -> s.equalsIgnoreCase(skill));
                })

                // sort
                .sorted((a, b) -> Double.compare(
                        b.getMatchScore() == null ? 0 : b.getMatchScore(),
                        a.getMatchScore() == null ? 0 : a.getMatchScore()
                ))

                // DTO mapping
                .map(this::mapToCandidateResponse)

                .toList();

        // =========================================================
        // Pagination
        // =========================================================
        int start = page * size;
        int end = Math.min(start + size, filtered.size());

        List<ApplicationCandidateResponse> paginated =
                start >= filtered.size()
                        ? List.of()
                        : filtered.subList(start, end);

        return new PageImpl<>(
                paginated,
                PageRequest.of(page, size),
                filtered.size()
        );
    }
    private String simplifySkill(String skill) {

        skill = normalize(skill);

        // Remove versions
        skill = skill.replaceAll("\\d+", "");

        // Remove common suffixes
        skill = skill.replace("js", "");

        // Remove plural forms
        if (skill.endsWith("s")) {
            skill = skill.substring(0, skill.length() - 1);
        }

        return skill;
    }

    @Override
    public Page<JobResponseDTO> getRecruiterJobs(int page, int size) {

        // =========================================================
        // ✅ Logged-in Recruiter
        // =========================================================

        User recruiter = getLoggedInUser();

        // =========================================================
        // ✅ Pagination
        // =========================================================

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("postedDate").descending()
        );

        // =========================================================
        // ✅ Fetch Recruiter Jobs
        // =========================================================

        Page<Job> jobs =
                jobRepo.findByRecruiter(recruiter, pageable);

        // =========================================================
        // ✅ Entity -> DTO Mapping
        // =========================================================

        return jobs.map(job -> {

            RecruiterDTO recruiterDTO = new RecruiterDTO();

            recruiterDTO.setPublicId(recruiter.getPublicId());
            recruiterDTO.setName(recruiter.getName());
            recruiterDTO.setEmail(recruiter.getEmail());

            JobResponseDTO dto =
                    new JobResponseDTO(job, recruiterDTO);

            // =====================================================
            // ✅ Applications Count
            // =====================================================

            long applicationsCount =
                    applicationRepo.countByJob(job);

            // if field exists
            dto.setApplicationsCount(applicationsCount);

            return dto;
        });
    }

    @Override
    public Page<ApplicationCandidateResponse> getApplicationsByJobForRecruiter(
            String jobId,
            String recruiterEmail,
            int page,
            int size
    ) {

        // 1. Get recruiter
        User recruiter = userRepo.findByEmail(recruiterEmail)
                .orElseThrow(() -> new RuntimeException("Recruiter not found"));

        // 2. Get job
        Job job = jobRepo.findByPublicId(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // 3. SECURITY CHECK (VERY IMPORTANT)
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new RuntimeException("You are not allowed to access this job applications");
        }

        Pageable pageable = PageRequest.of(page, size);

        // 4. Fetch applications
        Page<Application> applications =
                applicationRepo.findByJobPublicId(jobId, pageable);

        // 5. Map to DTO
        return applications.map(app -> ApplicationCandidateResponse.builder()
                .applicationId(app.getPublicId())

                // candidate info
                .studentPublicId(app.getCandidate().getPublicId())
                .candidateName(app.getCandidateName())
                .email(app.getCandidateEmail())

                // job info
                .jobPublicId(app.getJob().getPublicId())
                .jobTitle(app.getJob().getTitle())
                .companyName(app.getJob().getCompany().getName())

                // status + score
                .status(app.getStatus().name())
                .matchScore(app.getMatchScore())

                // ✅ FIXED SKILLS MAPPING
                .skills(
                        app.getSkills() == null
                                ? List.of()
                                : app.getSkills().stream()
                                .map(ApplicationSkill::getSkill) // or getSkill().getName()
                                .toList()
                )

                // ❌ REMOVE extraSkills (NOT IN ENTITY)
                .extraSkills(List.of()) // OR DELETE FIELD FROM DTO (recommended)

                // other fields
                .resumeUrl(app.getResumeUrl())
                .coverLetter(app.getCoverLetter())
                .viewed(app.getViewed())
                .appliedAt(app.getAppliedAt())

                .build()
        );
    }
}
