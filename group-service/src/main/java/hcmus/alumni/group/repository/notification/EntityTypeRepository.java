package hcmus.alumni.group.repository.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.group.common.NotificationType;
import hcmus.alumni.group.model.notification.EntityTypeModel;

public interface EntityTypeRepository extends JpaRepository<EntityTypeModel, Long> {
    Optional<EntityTypeModel> findByEntityTableAndNotificationType(String entityTable, NotificationType notificationType);
}
