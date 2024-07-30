package hcmus.alumni.userservice.utils;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.userservice.common.NotificationType;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.notification.EntityTypeModel;
import hcmus.alumni.userservice.model.notification.NotificationChangeModel;
import hcmus.alumni.userservice.model.notification.NotificationModel;
import hcmus.alumni.userservice.model.notification.NotificationObjectModel;
import hcmus.alumni.userservice.model.notification.StatusNotificationModel;
import hcmus.alumni.userservice.repository.notification.EntityTypeRepository;
import hcmus.alumni.userservice.repository.notification.NotificationChangeRepository;
import hcmus.alumni.userservice.repository.notification.NotificationObjectRepository;
import hcmus.alumni.userservice.repository.notification.NotificationRepository;

@Service
public class NotificationService {
	@Autowired
	private EntityTypeRepository entityTypeRepository;
	@Autowired
	private NotificationObjectRepository notificationObjectRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private NotificationChangeRepository notificationChangeRepository;

	@Autowired
	private FirebaseService firebaseService;

	@Transactional
	public void createNotification(String entityTable, NotificationType notificationType, 
		String entityId, UserModel actor, UserModel notifier, String notificationMessage, String parentId) {
		//Create Notification Object
		EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType(entityTable, notificationType)
			.orElseGet(() -> {
				EntityTypeModel newEntityType = new EntityTypeModel();
				newEntityType.setEntityTable(entityTable);
				newEntityType.setNotificationType(notificationType);
				return entityTypeRepository.save(newEntityType);
			});

		NotificationObjectModel notificationObject = new NotificationObjectModel();
		notificationObject.setEntityType(entityType);
		notificationObject.setEntityId(entityId);
		notificationObjectRepository.save(notificationObject);

		// Create Notification Change
		NotificationChangeModel notificationChange = new NotificationChangeModel();
		notificationChange.setNotificationObject(notificationObject);
		notificationChange.setActor(actor);
		notificationChangeRepository.save(notificationChange);

		// Create Notification
		NotificationModel notification = new NotificationModel();
		notification.setNotificationObject(notificationObject);
		notification.setNotifier(notifier);
		notification.setStatus(new StatusNotificationModel(1));
		notificationRepository.save(notification);

		// Send push notification
		firebaseService.sendNotification(
			notification, notificationChange, notificationObject,
			actor.getAvatarUrl(),
			notificationMessage,
			parentId);
	}
	
	@Transactional
	public void deleteNotification(String entityTable, NotificationType notificationType, String entityId) {
		Optional<EntityTypeModel> optionalET = entityTypeRepository.findByEntityTableAndNotificationType(entityTable, notificationType);
		if (optionalET.isEmpty()) return;
		EntityTypeModel entityType = optionalET.get();
		
		// Update isDelete for NotificationObject
		List<NotificationObjectModel> notificationObjects = notificationObjectRepository.findByEntityTypeAndEntityId(entityType, entityId);
		for (NotificationObjectModel notificationObject : notificationObjects) {
			notificationObject.setIsDelete(true);
			notificationObjectRepository.save(notificationObject);

			// Update isDelete for NotificationChange
			List<NotificationChangeModel> notificationChanges = notificationChangeRepository.findByNotificationObject(notificationObject);
			for (NotificationChangeModel notificationChange : notificationChanges) {
				notificationChange.setIsDelete(true);
				notificationChangeRepository.save(notificationChange);
			}
			
			// Update status for Notification
			List<NotificationModel> notifications = notificationRepository.findByNotificationObject(notificationObject);
			for (NotificationModel notification : notifications) {
				notification.setStatus(new StatusNotificationModel(3));
				notificationRepository.save(notification);
			}
		}
	}
}