package com.jobportal.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    @Value("${resend.from.email}")
    private String fromEmail;

    public EmailService(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendEmail(String to, String subject, String html) throws ResendException {

        SendEmailRequest request = SendEmailRequest.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(html)
                .build();

        resend.emails().send(request);
    }
}