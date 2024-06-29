package hcmus.alumni.event.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.event.model.notification.NotificationChangeModel;
import hcmus.alumni.event.model.notification.NotificationModel;
import hcmus.alumni.event.model.notification.NotificationObjectModel;
import hcmus.alumni.event.model.notification.StatusNotificationModel;
import hcmus.alumni.event.repository.notification.NotificationChangeRepository;
import hcmus.alumni.event.repository.notification.NotificationObjectRepository;
import hcmus.alumni.event.repository.notification.NotificationRepository;

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
}