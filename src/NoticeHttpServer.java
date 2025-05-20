import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class NoticeHttpServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/notices", new NoticeHandler());

        // Add CORS handler for OPTIONS requests
        server.createContext("/", new CorsHandler());

        server.setExecutor(null); // default executor
        System.out.println("🚀 Server started on port " + port);
        server.start();
    }

    // Global CORS handler for preflight requests
    static class CorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Not an OPTIONS request, return 404
            exchange.sendResponseHeaders(404, -1);
        }
    }

    static class NoticeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers to all responses
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            String method = exchange.getRequestMethod();

            switch (method.toUpperCase()) {
                case "OPTIONS":
                    // Handle preflight CORS request
                    exchange.sendResponseHeaders(204, -1);
                    break;
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    String error = "{\"error\":\"Unsupported method\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, error.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(error.getBytes());
                    }
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                List<Notice> notices = NoticeDAO.getAllNotices();

                JSONArray jsonArray = new JSONArray();
                for (Notice notice : notices) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", notice.getId());
                    obj.put("title", notice.getTitle());
                    obj.put("content", notice.getContent());
                    obj.put("category", notice.getCategory());
                    obj.put("created_at", notice.getCreatedAt());
                    obj.put("is_event", notice.isEvent());
                    obj.put("event_datetime", notice.getEventTime() != null ? notice.getEventTime().toString() : "");
                    jsonArray.put(obj);
                }

                String response = jsonArray.toString();
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

                // Debug output
                System.out.println("Sending " + notices.size() + " notices to client");

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }

            } catch (Exception e) {
                e.printStackTrace();
                String error = "{\"error\":\"Unable to fetch notices: " + e.getMessage() + "\"}";
                byte[] bytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        }


        private void handlePost(HttpExchange exchange) throws IOException {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }

                JSONObject json = new JSONObject(requestBody.toString());
                String title = json.getString("title");
                String content = json.getString("content");
                String category = json.optString("category", "General");

                // New fields for calendar event
                boolean isEvent = json.optBoolean("is_event", false);
                String eventDateTimeStr = json.optString("event_datetime", "");
                Timestamp eventTime = null;
                if (!eventDateTimeStr.isEmpty()) {
                    LocalDateTime localDateTime = LocalDateTime.parse(eventDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    eventTime = Timestamp.valueOf(localDateTime);
                }

                // Create and save notice
                Notice notice = new Notice(title, content, category);
                notice.setEvent(isEvent);
                notice.setEventTime(eventTime);


                NoticeDAO noticeDAO = new NoticeDAO();
                noticeDAO.addNotice(notice);

                // Try to send push notification but don't fail if it doesn't work
                try {
                    FCMSender.sendPushNotification(title, content);
                    System.out.println("Push notification sent successfully");
                } catch (Exception e) {
                    System.out.println("Failed to send push notification: " + e.getMessage());
                }

                String response = "{\"message\":\"Notice added successfully\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
                String error = "{\"error\":\"Failed to add notice: " + e.getMessage() + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        }
    }
}