package hcmus.alumni.news.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.news.model.notification.NotificationChangeModel;

public interface NotificationChangeRepository extends JpaRepository<NotificationChangeModel, Long> {}

