package com.jobportal.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeExtractorService {

    private final Tika tika = new Tika();

    public String extractText(MultipartFile file) {

        try {

            return tika.parseToString(file.getInputStream());

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to extract resume text",
                    e
            );
        }
    }
}