import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class FCMSender {

    static {
        try {
            FileInputStream serviceAccount = new FileInputStream("./serviceAccountKey.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed:");
            e.printStackTrace();
        }
    }

    public static void sendPushNotification(String title, String body) {
        try {
            // Optional: send both notification + data
            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("title", title);
            dataPayload.put("body", body);

            Message message = Message.builder()
                    .setTopic("all")
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(dataPayload)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ Notification sent: " + response);
        } catch (Exception e) {
            System.out.println("❌ Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
