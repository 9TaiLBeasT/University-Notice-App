import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class FCMSender {

    static {
        try {
            String json = System.getenv("FIREBASE_CREDENTIALS_JSON");
            if (json == null || json.isEmpty()) {
                throw new RuntimeException("Missing FIREBASE_CREDENTIALS_JSON env var");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))))
                    .build();


            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            //System.out.println("✅ Firebase initialized with " + path);

        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed:");
            e.printStackTrace();
        }
    }

    public static void sendPushNotification(String title, String body) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setTopic("all")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Notification sent: " + response);
        } catch (Exception e) {
            System.out.println("❌ Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
