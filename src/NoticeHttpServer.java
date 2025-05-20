import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class NoticeHttpServer {

    public static void main(String[] args) throws IOException {
        // ✅ Fix: Bind to all interfaces (required for Render/Docker)
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "10000"));

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/notices", new NoticeHandler());

        // Add a health check endpoint
        server.createContext("/health", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"status\":\"UP\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        // Add a database test endpoint
        server.createContext("/test-db", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Content-Type", "application/json");

                try {
                    Connection conn = DBConnection.getConnection();
                    conn.close();
                    String response = "{\"status\":\"Database connection successful\"}";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (Exception e) {
                    String errorMessage = e.getMessage();
                    String response = "{\"status\":\"Database connection failed\",\"error\":\"" +
                            errorMessage.replace("\"", "\\\"") + "\"}";
                    exchange.sendResponseHeaders(500, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        });

        server.setExecutor(null); // default executor
        System.out.println("🌐 Starting server on port " + port);
        System.out.println("🛠 PORT from environment = " + System.getenv("PORT"));
        server.start();
        System.out.println("🌐 Listening on http://0.0.0.0:" + port);
    }

    static class NoticeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers to all responses
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

            String method = exchange.getRequestMethod();

            // Handle preflight requests
            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            System.out.println("Received " + method + " request to /notices");

            switch (method.toUpperCase()) {
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
                System.out.println("GET /notices - Attempting to fetch notices...");
                List<Notice> notices = NoticeDAO.getAllNotices();
                System.out.println("Successfully fetched " + notices.size() + " notices");

                JSONArray jsonArray = new JSONArray();
                for (Notice notice : notices) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", notice.getId());
                    obj.put("title", notice.getTitle());
                    obj.put("content", notice.getContent());
                    obj.put("category", notice.getCategory());
                    obj.put("created_at", notice.getCreatedAt());

                    // ➕ Include event fields
                    obj.put("is_event", notice.isEvent());
                    obj.put("event_datetime", notice.getEventTime() != null ? notice.getEventTime().toString() : "");

                    jsonArray.put(obj);
                }

                String response = jsonArray.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                System.err.println("ERROR in /notices: " + e.getMessage());
                e.printStackTrace();
                String error = "{\"error\":\"Unable to fetch notices: " + e.getMessage().replace("\"", "\\\"") + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
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

                System.out.println("POST /notices - Request body: " + requestBody);

                JSONObject json = new JSONObject(requestBody.toString());
                String title = json.getString("title");
                String content = json.getString("content");
                String category = json.optString("category", "General");

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

                // Send push notification
                try {
                    FCMSender.sendPushNotification(title, content);
                    System.out.println("✅ Push notification sent");
                } catch (Exception e) {
                    System.err.println("❌ Failed to send push notification: " + e.getMessage());
                    e.printStackTrace();
                }

                String response = "{\"message\":\"Notice added and push notification sent\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                System.err.println("ERROR in POST /notices: " + e.getMessage());
                e.printStackTrace();
                String error = "{\"error\":\"Failed to add notice: " + e.getMessage().replace("\"", "\\\"") + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        }
    }
}