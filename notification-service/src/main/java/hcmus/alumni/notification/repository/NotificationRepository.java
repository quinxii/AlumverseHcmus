package hcmus.alumni.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.notification.model.NotificationModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, String> {
	@Query(value = "select distinct p.name from role_permission rp " +
		"join role r on r.id = rp.role_id and r.is_delete = false " +
		"join permission p on p.id = rp.permission_id and p.is_delete = false " +
		"where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
	 List<String> getPermissions(List<String> role, String domain);
}