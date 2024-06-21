package hcmus.alumni.notification.dto;

import java.util.Date;

public interface INotificationDto {
	interface User {
		String getId();
		String getFullName();
		String getAvatarUrl()
;	}
	interface Status {
		String getName();
	}
	
	String getId(); //notification_object_id
	User getNotifier();
	User getActor();
	String getEntityId();
	String getEntityTable();
	String getNotificationType();
	String getCreateAt();
	Status getStatus();
	String getNotificationImageUrl();
	String getNotificationMessage();
	String getParentId(); //optional
}