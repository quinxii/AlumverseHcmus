package hcmus.alumni.news.repository.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import hcmus.alumni.news.common.NotificationType;
import hcmus.alumni.news.model.notification.EntityTypeModel;

public interface EntityTypeRepository extends JpaRepository<EntityTypeModel, Long> {
    Optional<EntityTypeModel> findByEntityTableAndNotificationType(String entityTable, NotificationType notificationType);
}
