package hcmus.alumni.event.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.notification.NotificationModel;
import hcmus.alumni.event.model.notification.NotificationObjectModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
	List<NotificationModel> findByNotificationObject(NotificationObjectModel notificationObject);
}