package hcmus.alumni.group.repository.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.UserModel;
import hcmus.alumni.group.model.notification.NotificationChangeModel;
import hcmus.alumni.group.model.notification.NotificationObjectModel;

public interface NotificationChangeRepository extends JpaRepository<NotificationChangeModel, Long> {
	@Transactional
	@Modifying
	@Query("UPDATE NotificationChangeModel nc SET nc.actor.id = :actorId WHERE nc.notificationObject.id = :notificationObjectId")
	void updateActorIdByNotificationObject(@Param("notificationObjectId") Long notificationObjectId, @Param("actorId") String actorId);
	
	List<NotificationChangeModel> findByNotificationObject(NotificationObjectModel notificationObject);
	
	@Query("SELECT nc FROM NotificationChangeModel nc JOIN nc.notificationObject no " +
			"WHERE no.entityId = :entityId AND nc.actor.id = :actorId AND nc.isDelete = false")
	Optional<NotificationChangeModel> findByEntityIdAndActorId(String entityId, String actorId);
}

