package com.jobportal.service;

import com.jobportal.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeAsyncTaskService {

    private final ResumeExtractorService resumeExtractorService;
    private final StudentProfileRepository studentProfileRepository;

    // ✅ Run on a background thread pool, freeing the main HTTP thread
    @Async
    public void extractTextInBackground(Long profileId, MultipartFile resume) {
        try {
            log.info("Starting background resume extraction for Profile ID: {}", profileId);

            String extractedText = resumeExtractorService.extractText(resume);

            studentProfileRepository.updateResumeText(profileId, extractedText);

            log.info("Successfully saved extracted text for Profile ID: {}", profileId);
        } catch (Exception e) {
            log.error("Failed to extract resume text asynchronously for Profile ID: " + profileId, e);
        }
    }
}