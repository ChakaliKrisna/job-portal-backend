package com.jobportal.service;

//package com.jobportal.service;

import com.jobportal.entity.StudentProfile;
import com.jobportal.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeExtractionService {

    private final StudentProfileRepository studentProfileRepository;

    @Async
    public void extractResumeText(Long profileId, byte[] pdfBytes) {

        try {

            String extractedText = "";

            try (PDDocument document =
                         Loader.loadPDF(pdfBytes)) {

                PDFTextStripper stripper =
                        new PDFTextStripper();

                extractedText =
                        stripper.getText(document);
            }

            StudentProfile profile =
                    studentProfileRepository
                            .findById(profileId)
                            .orElseThrow();

            profile.setResumeText(extractedText);

            studentProfileRepository.save(profile);

            System.out.println("Resume text extracted successfully");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}