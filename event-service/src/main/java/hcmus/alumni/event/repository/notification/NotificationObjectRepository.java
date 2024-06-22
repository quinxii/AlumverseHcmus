package hcmus.alumni.event.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.notification.NotificationObjectModel;

public interface NotificationObjectRepository extends JpaRepository<NotificationObjectModel, Long> {}

