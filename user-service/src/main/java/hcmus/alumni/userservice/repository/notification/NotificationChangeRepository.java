package hcmus.alumni.userservice.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.notification.NotificationChangeModel;
import hcmus.alumni.userservice.model.notification.NotificationObjectModel;

public interface NotificationChangeRepository extends JpaRepository<NotificationChangeModel, Long> {
	List<NotificationChangeModel> findByNotificationObject(NotificationObjectModel notificationObject);
}

