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
                System.err.println("❌ FIREBASE_CREDENTIALS_JSON is missing or empty.");
                throw new IllegalStateException("Missing FIREBASE_CREDENTIALS_JSON environment variable");
            }

            System.out.println("📦 Credentials JSON loaded from env. Length: " + json.length());

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
                    ))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ FirebaseApp initialized successfully.");
            } else {
                System.out.println("ℹ️ FirebaseApp already initialized.");
            }

        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed:");
            e.printStackTrace();
        }
    }

    public static void sendPushNotification(String title, String body) {
        try {
            System.out.println("📤 Sending push notification...");
            System.out.println("📨 Title: " + title);
            System.out.println("📨 Body: " + body);

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
