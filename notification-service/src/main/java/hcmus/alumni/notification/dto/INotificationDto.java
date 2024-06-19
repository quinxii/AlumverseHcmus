package hcmus.alumni.notification.dto.response;

import java.util.Date;

public interface INotificationDto {
	interface User {
		String getId();
		String getFullName();
	}
	
	String getId(); //notification_object_id
	User getNotifier();
	User getActor();
	String getEntityId();
	String getEntityTable();
	String getNotificationType();
	String getCreatedOn();
	String getUrl();
	String getNotificationMessage();
}