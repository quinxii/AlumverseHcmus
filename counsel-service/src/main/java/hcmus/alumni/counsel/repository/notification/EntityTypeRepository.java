package hcmus.alumni.counsel.repository.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.counsel.common.NotificationType;
import hcmus.alumni.counsel.model.notification.EntityTypeModel;

public interface EntityTypeRepository extends JpaRepository<EntityTypeModel, Long> {
    Optional<EntityTypeModel> findByEntityTableAndNotificationType(String entityTable, NotificationType notificationType);
}
