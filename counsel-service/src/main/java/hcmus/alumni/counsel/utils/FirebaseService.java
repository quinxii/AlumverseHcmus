package hcmus.alumni.counsel.utils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;

import hcmus.alumni.counsel.model.notification.NotificationChangeModel;
import hcmus.alumni.counsel.model.notification.NotificationModel;
import hcmus.alumni.counsel.model.notification.NotificationObjectModel;
import hcmus.alumni.counsel.repository.UserSubscriptionTokenRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
	@Autowired
    private UserSubscriptionTokenRepository userSubscriptionTokenRepository;
	
	public void sendCommentNotification(
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
            MulticastMessage message = MulticastMessage.builder()
                    .putData("title", "Alumverse")
                    .putData("body", payload.toString())
                    .addAllTokens(tokens)
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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

        public NotificationPayload(String id, String notifierId, String actorId, String entityId, String entityTable, String notificationType, String notificationImageUrl, String notificationMessage, String parentId) {
            this.id = id;
            this.notifierId = notifierId;
            this.actorId = actorId;
            this.entityId = entityId;
            this.entityTable = entityTable;
            this.notificationType = notificationType;
            this.notificationImageUrl = notificationImageUrl;
            this.notificationMessage = notificationMessage;
            this.parentId = parentId;
        }

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

