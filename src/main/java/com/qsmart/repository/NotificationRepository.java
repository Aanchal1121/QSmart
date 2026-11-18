package com.qsmart.repository;

import com.qsmart.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByPhoneOrderByCreatedAtDesc(String phone);
    List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
