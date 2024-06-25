package hcmus.alumni.counsel.repository.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.counsel.model.notification.EntityTypeModel;
import hcmus.alumni.counsel.model.notification.NotificationObjectModel;

public interface NotificationObjectRepository extends JpaRepository<NotificationObjectModel, Long> {
	Optional<NotificationObjectModel> findByEntityTypeAndEntityId(EntityTypeModel entityType, String entityId);
}

