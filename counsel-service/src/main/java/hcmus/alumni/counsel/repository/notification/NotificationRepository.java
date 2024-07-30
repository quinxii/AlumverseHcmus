package hcmus.alumni.counsel.repository.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.counsel.model.notification.NotificationModel;
import hcmus.alumni.counsel.model.notification.NotificationObjectModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
	@Transactional
	@Modifying
	@Query("UPDATE NotificationModel n SET n.status.id = :statusId WHERE n.notificationObject.id = :notificationObjectId")
	void updateStatusByNotificationObject(@Param("notificationObjectId") Long notificationObjectId, @Param("statusId") int statusId);
	
	List<NotificationModel> findByNotificationObject(NotificationObjectModel notificationObject);
}