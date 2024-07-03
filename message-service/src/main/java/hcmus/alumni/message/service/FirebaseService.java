package hcmus.alumni.message.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hcmus.alumni.message.model.MessageModel;
import hcmus.alumni.message.repostiory.UserSubscriptionTokenRepository;

@Service
public class FirebaseService {
	@Autowired
	private UserSubscriptionTokenRepository userSubscriptionTokenRepository;
	
	public void sendChatMessageNotification(
			MessageModel msg,
			List<String> userIds) {

		String notificationMessage = null;
		switch (msg.getMessageType().toString()) {
			case "TEXT":
				notificationMessage = msg.getContent();
				break;
			case "IMAGE":
				notificationMessage = "đã gửi một hình ảnh";
				break;
			case "FILE":
				notificationMessage = "đã gửi một tệp tin";
				break;
			case "VIDEO":
				notificationMessage = "đã gửi một video";
				break;
			case "SOUND":
				notificationMessage = "đã gửi một tin nhắn thoại";
				break;
		}
		NotificationPayload payload = new NotificationPayload(
				null,
				null,
				msg.getSender().getId(),
				msg.getId().toString(),
				"message",
				"CREATE",
				msg.getSender().getAvatarUrl(),
				notificationMessage,
				msg.getInbox().getId().toString()
		);
		
		for (String userId : userIds) {
			if (userId.equals(msg.getSender().getId())) continue;
			List<String> tokens = userSubscriptionTokenRepository.findAllTokensByUserId(userId);
			if (!tokens.isEmpty()) {
				payload.setNotifierId(userId);
				for (String token : tokens) {
					Message message = Message.builder()
						.setNotification(Notification.builder()
					            .setTitle(msg.getSender().getFullName())
					            .setBody(notificationMessage)
					            .build())
						.putData("body", payload.toString())
						.setToken(token)
						.build();
					try {
						FirebaseMessaging.getInstance().send(message);
					} catch (Exception e) {
					    e.printStackTrace();
					}
				}
			}
		}
    }

	@Data
	@AllArgsConstructor
	public static class NotificationPayload {
		private String id;
		private String notifierId;
		private String actorId;
		private String entityId;
		private String entityTable;
		private String notificationType;
		private String notificationImageUrl;
		private String notificationMessage;
		private String parentId;
	
		@Override
		public String toString() {
			return "{" +
				"\"id\":\"" + id + "\"," +
				"\"notifierId\":\"" + notifierId + "\"," +
				"\"actorId\":\"" + actorId + "\"," +
				"\"entityId\":\"" + entityId + "\"," +
				"\"entityTable\":\"" + entityTable + "\"," +
				"\"notificationType\":\"" + notificationType + "\"," +
				"\"notificationImageUrl\":\"" + notificationImageUrl + "\"," +
				"\"notificationMessage\":\"" + notificationMessage + "\"," +
				"\"parentId\":\"" + parentId + "\"" +
				"}";
		}
	}
}

