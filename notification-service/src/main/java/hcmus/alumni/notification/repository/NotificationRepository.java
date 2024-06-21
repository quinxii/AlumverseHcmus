package hcmus.alumni.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.notification.model.notification.NotificationModel;
import hcmus.alumni.notification.dto.INotificationDto;

public interface NotificationRepository extends JpaRepository<NotificationModel, String> {
	@Query(value = "select distinct p.name from role_permission rp " +
		"join role r on r.id = rp.role_id and r.is_delete = false " +
		"join permission p on p.id = rp.permission_id and p.is_delete = false " +
		"where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
	 List<String> getPermissions(List<String> role, String domain);
	
	@Query("SELECT COUNT(n) " +
		"FROM NotificationModel n " +
		"WHERE n.status.id = 1 " +
		"AND n.notifier.id = :userId")
	int getUnreadNotificationsCount(@Param("userId") String userId);
	
	@Query("SELECT no.id AS id, n.notifier AS notifier, nc.actor AS actor, " +
		"no.entityId AS entityId, et.entityTable AS entityTable, et.notificationType AS notificationType, " + 
		"no.createAt AS createAt, n.status AS status " +
		"FROM NotificationModel n " +
		"JOIN n.notificationObject no " +
		"JOIN no.entityType et " +
		"JOIN NotificationChangeModel nc ON nc.notificationObject.id = no.id " +
		"WHERE no.entityType.id = et.id " +
		"AND n.status.id != 3 " +
		"AND n.notifier.id = :userId " + 
		"ORDER BY no.createAt DESC")
	Page<INotificationDto> getNotifications(@Param("userId") String userId, Pageable pageable);
}