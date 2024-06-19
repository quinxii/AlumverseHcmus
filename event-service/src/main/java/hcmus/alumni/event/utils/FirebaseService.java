package hcmus.alumni.event.utils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;

import hcmus.alumni.event.repository.UserSubscriptionTokenRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
	@Autowired
    private UserSubscriptionTokenRepository userSubscriptionTokenRepository;
	
    public void sendNotification(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .putData("title", title)
                .putData("body", body)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendUserNotification(String userId, String title, String body) {
        List<String> tokens = userSubscriptionTokenRepository.findAllTokensByUserId(userId);

        if (!tokens.isEmpty()) {
            MulticastMessage message = MulticastMessage.builder()
                    .putData("title", title)
                    .putData("body", body)
                    .addAllTokens(tokens)
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
                System.out.println("Successfully sent message: " + response.getSuccessCount() + " messages were sent successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

