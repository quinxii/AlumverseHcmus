package hcmus.alumni.userservice.repository.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.userservice.common.NotificationType;
import hcmus.alumni.userservice.model.notification.EntityTypeModel;

public interface EntityTypeRepository extends JpaRepository<EntityTypeModel, Long> {
    Optional<EntityTypeModel> findByEntityTableAndNotificationType(String entityTable, NotificationType notificationType);
}
