package hcmus.alumni.userservice.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.model.notification.EntityTypeModel;
import hcmus.alumni.userservice.model.notification.NotificationObjectModel;

public interface NotificationObjectRepository extends JpaRepository<NotificationObjectModel, Long> {
	List<NotificationObjectModel> findByEntityTypeAndEntityId(EntityTypeModel entityType, String entityId);
}

