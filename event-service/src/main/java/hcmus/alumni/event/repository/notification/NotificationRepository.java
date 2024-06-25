package hcmus.alumni.event.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.notification.NotificationModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {}

