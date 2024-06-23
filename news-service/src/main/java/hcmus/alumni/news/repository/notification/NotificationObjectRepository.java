package hcmus.alumni.news.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.news.model.notification.NotificationObjectModel;

public interface NotificationObjectRepository extends JpaRepository<NotificationObjectModel, Long> {}

