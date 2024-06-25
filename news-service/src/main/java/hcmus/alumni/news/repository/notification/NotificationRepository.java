package hcmus.alumni.news.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.news.model.notification.NotificationModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {}

