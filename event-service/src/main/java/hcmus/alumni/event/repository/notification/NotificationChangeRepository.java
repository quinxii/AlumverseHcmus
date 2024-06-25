package hcmus.alumni.event.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.notification.NotificationChangeModel;

public interface NotificationChangeRepository extends JpaRepository<NotificationChangeModel, Long> {}

