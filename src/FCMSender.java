
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMSender {

    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";

    // 🔑 Replace with your **actual** FCM Server Key from Firebase > Project Settings > Cloud Messaging
    private static final String SERVER_KEY = "key=BKQv3nlwoRJm87d9o6g5Swxs8jj_dKZbqO3HGEC2BI6-_-Vq5x4NGGQeADMAqtOvLXAWfNv34gRMRMJVBEY0OB4";

    public static void sendPushNotification(String title, String body) {
        try {
            URL url = new URL(FCM_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");

            // 🔔 Notification payload
            String payload = "{"
                    + "\"to\":\"/topics/all\","
                    + "\"notification\":{"
                    + "\"title\":\"" + title + "\","
                    + "\"body\":\"" + body + "\""
                    + "}"
                    + "}";

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(payload.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = conn.getResponseCode();
            System.out.println("FCM Response Code: " + responseCode);

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
