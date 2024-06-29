package hcmus.alumni.event.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.model.notification.NotificationChangeModel;
import hcmus.alumni.event.model.notification.NotificationObjectModel;

public interface NotificationChangeRepository extends JpaRepository<NotificationChangeModel, Long> {
	List<NotificationChangeModel> findByNotificationObject(NotificationObjectModel notificationObject);
}