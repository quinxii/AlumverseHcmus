package hcmus.alumni.counsel.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.counsel.model.notification.NotificationChangeModel;

public interface NotificationChangeRepository extends JpaRepository<NotificationChangeModel, Long> {
	@Transactional
	@Modifying
	@Query("UPDATE NotificationChangeModel nc SET nc.actor.id = :actorId WHERE nc.notificationObject.id = :notificationObjectId")
	void updateActorIdByNotificationObject(@Param("notificationObjectId") Long notificationObjectId, @Param("actorId") String actorId);
}

