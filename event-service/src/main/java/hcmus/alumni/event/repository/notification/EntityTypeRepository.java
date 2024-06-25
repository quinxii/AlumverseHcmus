package hcmus.alumni.event.repository.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.event.common.NotificationType;
import hcmus.alumni.event.model.notification.EntityTypeModel;

public interface EntityTypeRepository extends JpaRepository<EntityTypeModel, Long> {
    Optional<EntityTypeModel> findByEntityTableAndNotificationType(String entityTable, NotificationType notificationType);
}
