package com.jobportal.service;

//package com.jobportal.service;

import com.jobportal.entity.*;
import com.jobportal.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(
            User user,
            String title,
            String message,
            NotificationType type
    ) {

        Notification notification = new Notification();

        notification.setUser(user);

        notification.setTitle(title);

        notification.setMessage(message);

        notification.setType(type);

        notificationRepository.save(notification);
    }
}