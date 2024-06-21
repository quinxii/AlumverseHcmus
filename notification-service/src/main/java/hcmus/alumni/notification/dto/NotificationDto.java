package hcmus.alumni.notification.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NotificationDto {
	@Data
	public static class User {
		private String id;
        private String fullName;
        private String avatarUrl;
;	}
	@Data
	public static class Status {
		private String name;
	}
	
	private String id;
	private User notifier;
	private User actor;
	private String entityId;
	private String entityTable;
	private String notificationType;
	private String createAt;
	private Status status;
	private String notificationImageUrl;
	private String notificationMessage;
	private String parentId;
}