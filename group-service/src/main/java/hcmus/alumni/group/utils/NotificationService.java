package hcmus.alumni.group.utils;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.notification.NotificationChangeModel;
import hcmus.alumni.group.model.notification.NotificationModel;
import hcmus.alumni.group.model.notification.NotificationObjectModel;
import hcmus.alumni.group.model.notification.StatusNotificationModel;
import hcmus.alumni.group.repository.notification.NotificationChangeRepository;
import hcmus.alumni.group.repository.notification.NotificationObjectRepository;
import hcmus.alumni.group.repository.notification.NotificationRepository;

@Service
public class NotificationService {
	@Autowired
	private NotificationObjectRepository notificationObjectRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private NotificationChangeRepository notificationChangeRepository;
	
	@Transactional
	public void deleteNotificationsByEntityIds(List<String> entityIds) {
		List<NotificationObjectModel> notificationObjects = notificationObjectRepository.findByEntityIdIn(entityIds);
		
		for (NotificationObjectModel notificationObject : notificationObjects) {
			// Update isDelete for NotificationObject
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
	
	@Transactional
	public void deleteRequestJoinNotifications(String groupId, String requestUserId) {
		Optional<NotificationChangeModel> optionalNotificationChange = notificationChangeRepository.findByEntityIdAndActorId(groupId, requestUserId);
		
		// Update isDelete for NotificationChange
		NotificationChangeModel notificationChange = optionalNotificationChange.get();
		notificationChange.setIsDelete(true);
		notificationChangeRepository.save(notificationChange);
		
		// Update isDelete for NotificationObject
		NotificationObjectModel notificationObject = notificationChange.getNotificationObject();
		notificationObject.setIsDelete(true);
		notificationObjectRepository.save(notificationObject);
		
		// Update status for Notification
		List<NotificationModel> notifications = notificationRepository.findByNotificationObject(notificationObject);
		for (NotificationModel notification : notifications) {
			notification.setStatus(new StatusNotificationModel(3));
			notificationRepository.save(notification);
		}
	}
}