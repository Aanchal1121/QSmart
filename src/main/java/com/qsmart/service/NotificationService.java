package com.qsmart.service;

import com.qsmart.model.Notification;
import com.qsmart.model.User;
import com.qsmart.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Notification notify(User user, String phone, String title, String message, String type, String channel) {
        Notification notification = new Notification(user, phone, title, message, type, channel);
        Notification saved = notificationRepository.save(notification);

        // Broadcast to WebSocket user channel if user is present
        try {
            if (user != null && user.getId() != null) {
                messagingTemplate.convertAndSend("/topic/user/" + user.getId() + "/notifications", saved);
            }
        } catch (Exception ignored) {
            // WebSocket might not be connected for user yet
        }

        return saved;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }
}
