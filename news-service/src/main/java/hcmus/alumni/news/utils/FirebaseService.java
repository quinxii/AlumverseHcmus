package hcmus.alumni.news.utils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import hcmus.alumni.news.model.notification.NotificationChangeModel;
import hcmus.alumni.news.model.notification.NotificationModel;
import hcmus.alumni.news.model.notification.NotificationObjectModel;
import hcmus.alumni.news.repository.UserSubscriptionTokenRepository;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
	@Autowired
    private UserSubscriptionTokenRepository userSubscriptionTokenRepository;
	
	public void sendNotification(
			NotificationModel notification, 
			NotificationChangeModel notificationChange, 
			NotificationObjectModel notificationObject, 
			String notificationImageUrl, 
			String notificationMessage, 
			String parentId) {

		NotificationPayload payload = new NotificationPayload(
		        notification.getId().toString(),
		        notification.getNotifier().getId(),
		        notificationChange.getActor().getId(),
		        notificationObject.getEntityId(),
		        notificationObject.getEntityType().getEntityTable(),
		        notificationObject.getEntityType().getNotificationType().toString(),
		        notificationImageUrl,
		        notificationMessage,
		        parentId
		);
		
		List<String> tokens = userSubscriptionTokenRepository.findAllTokensByUserId(notification.getNotifier().getId());
		if (!tokens.isEmpty()) {
			for (String token : tokens) {
				Message message = Message.builder()
					.setNotification(Notification.builder()
				            .setTitle("Alumverse")
				            .setBody(notificationMessage)
				            .build())
					.putData("body", payload.toString())
					.setToken(token)
					.build();
				try {
					String response = FirebaseMessaging.getInstance().send(message);
				} catch (Exception e) {
				    e.printStackTrace();
				}
			}
		}
    }
	
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

