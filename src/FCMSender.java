import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class FCMSender {

    static {
        try {
            System.out.println("🔄 Initializing Firebase...");
            String json = System.getenv("FIREBASE_CREDENTIALS_JSON");

            if (json == null || json.isEmpty()) {
                System.err.println("❌ Missing FIREBASE_CREDENTIALS_JSON env variable.");
                throw new IllegalStateException("Missing FIREBASE_CREDENTIALS_JSON");
            }

            System.out.println("📦 Credentials JSON loaded from env. Length: " + json.length());

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
                    ))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully from env var.");
            }

        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed:");
            e.printStackTrace();
        }
    }

    public static void sendPushNotification(String title, String body) {
        try {
            System.out.println("📤 Attempting to send FCM push...");
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setTopic("all")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Push notification sent successfully. Response: " + response);

        } catch (Exception e) {
            System.err.println("❌ Failed to send push notification:");
            e.printStackTrace();
        }
    }
}
