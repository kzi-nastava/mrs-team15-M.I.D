package rs.ac.uns.ftn.asd.ridenow.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Cloud Messaging (FCM) Configuration
 *
 * This configuration initializes the Firebase Admin SDK for sending push notifications
 * to mobile devices. If credentials are not found, Firebase is skipped gracefully.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    /**
     * Initialize Firebase Admin SDK
     *
     * The credentials file should be placed at: src/main/resources/firebase-credentials.json
     *
     * @return FirebaseMessaging bean for sending notifications, or null if credentials not available
     */
    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            // Check if Firebase is already initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                logger.info("Firebase already initialized");
                return FirebaseMessaging.getInstance();
            }

            // Try to load credentials from classpath
            InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("firebase-credentials.json");

            if (serviceAccount == null) {
                logger.warn("Firebase credentials file not found at src/main/resources/firebase-credentials.json");
                logger.warn("FCM notifications will NOT be available. To enable:");
                logger.warn("1. Download service account key from Firebase Console");
                logger.warn("2. Place it at: src/main/resources/firebase-credentials.json");
                logger.warn("3. Restart the application");
                return null;
            }

            // Initialize Firebase with credentials
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            FirebaseApp.initializeApp(options);
            logger.info("Firebase Admin SDK initialized successfully - FCM notifications enabled");

            return FirebaseMessaging.getInstance();

        } catch (IOException e) {
            logger.error("Error initializing Firebase Admin SDK: {}", e.getMessage(), e);
            logger.warn("FCM notifications will NOT be available");
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error initializing Firebase: {}", e.getMessage(), e);
            logger.warn("FCM notifications will NOT be available");
            return null;
        }
    }
}
