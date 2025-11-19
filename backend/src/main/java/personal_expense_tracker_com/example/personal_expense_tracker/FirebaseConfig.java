package personal_expense_tracker_com.example.personal_expense_tracker;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Starting Firebase initialization...");
            
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-service-account.json");
            if (serviceAccount == null) {
                logger.error("firebase-service-account.json not found in resources");
                throw new RuntimeException("firebase-service-account.json not found in resources");
            }
            
            logger.info("Firebase service account file found, creating options...");
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("No Firebase apps found, initializing new app...");
                FirebaseApp.initializeApp(options);
                logger.info("Firebase app initialized successfully");
            } else {
                logger.info("Firebase app already exists, skipping initialization");
            }
            
            serviceAccount.close();
            logger.info("Firebase initialization completed successfully");
        } catch (IOException e) {
            logger.error("ERROR: Failed to initialize Firebase: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        } catch (Exception e) {
            logger.error("ERROR: Unexpected error during Firebase initialization: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
} 