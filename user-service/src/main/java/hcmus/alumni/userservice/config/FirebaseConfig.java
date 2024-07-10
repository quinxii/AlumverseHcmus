package hcmus.alumni.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service.account.path}")
    private String serviceAccountPath;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            // Initialize Firebase only if there are no existing apps
            FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            return FirebaseApp.initializeApp(options);
        } else {
            // Firebase already initialized, return existing instance
            return FirebaseApp.getInstance();
        }
    }
}

