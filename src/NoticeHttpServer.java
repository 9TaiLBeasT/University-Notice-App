import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class NoticeHttpServer {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "10000"));
        System.out.println("🌐 Starting server on port " + port);

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/notices", new NoticeHandler());
        server.createContext("/signup", new AuthHandler());
        server.createContext("/login", new AuthHandler());

        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"UP\"}";
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.createContext("/test-db", exchange -> {
            addCorsHeaders(exchange);
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            String response;
            int statusCode;

            try {
                long start = System.currentTimeMillis();
                Connection conn = DBConnection.getConnection();
                long end = System.currentTimeMillis();
                conn.close();

                response = "{\"status\":\"Database connected\",\"time_ms\":" + (end - start) + "}";
                statusCode = 200;
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage().replace("\"", "\\\"") : "Unknown";
                response = "{\"status\":\"DB failed\",\"error\":\"" + errorMessage + "\"}";
                statusCode = 500;
            }

            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(null);
        System.out.println("🛠 Listening on http://0.0.0.0:" + port);
        server.start();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    static class NoticeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            String method = exchange.getRequestMethod();
            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            switch (method.toUpperCase()) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange);
                case "PUT" -> handlePut(exchange);
                default -> sendError(exchange, 405, "Unsupported method");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                String query = exchange.getRequestURI().getQuery();
                String role = "general";
                if (query != null && query.contains("role=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("role=")) {
                            role = param.split("=")[1].toLowerCase();
                            break;
                        }
                    }
                }

                List<Notice> notices = NoticeDAO.getNoticesForRole(role);

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
                    obj.put("file_url", notice.getFileUrl() != null ? notice.getFileUrl() : "");
                    jsonArray.put(obj);
                }

                byte[] responseBytes = jsonArray.toString().getBytes();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Failed to fetch notices: " + e.getMessage());
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
                boolean isEvent = json.optBoolean("is_event", false);
                String eventDateTimeStr = json.optString("event_datetime", "");
                String fileUrl = json.optString("file_url", null); // ✅ file url

                Timestamp eventTime = null;
                if (!eventDateTimeStr.isEmpty()) {
                    LocalDateTime dt = LocalDateTime.parse(eventDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    eventTime = Timestamp.valueOf(dt);
                }

                Notice notice = new Notice(title, content, category);
                notice.setEvent(isEvent);
                notice.setEventTime(eventTime);
                notice.setFileUrl(fileUrl); // ✅ set file url

                new NoticeDAO().addNotice(notice);

                try {
                    FCMSender.sendPushNotification(title, content);
                } catch (Exception e) {
                    System.err.println("❌ Push failed: " + e.getMessage());
                    e.printStackTrace();
                }

                String response = "{\"message\":\"Notice added and notification sent\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Failed to add notice: " + e.getMessage());
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            try {
                String path = exchange.getRequestURI().getPath(); // /notices/{id}
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    sendError(exchange, 400, "Invalid URL format. Expected /notices/{id}");
                    return;
                }

                int id = Integer.parseInt(parts[2]);

                boolean deleted = new NoticeDAO().deleteNotice(id);

                String response = deleted
                        ? "{\"message\":\"Deleted successfully\"}"
                        : "{\"error\":\"Notice not found or not deleted\"}";

                int statusCode = deleted ? 200 : 404;
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(statusCode, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendError(exchange, 500, "Failed to delete notice: " + e.getMessage());
            }
        }

        private void handlePut(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length != 3) {
                sendError(exchange, 400, "Invalid path");
                return;
            }

            int id = Integer.parseInt(parts[2]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }

            JSONObject json = new JSONObject(body.toString());

            String title = json.optString("title", null);
            String content = json.optString("content", null);
            String category = json.optString("category", null);
            boolean isEvent = json.optBoolean("is_event", false);
            String eventDateTimeStr = json.optString("event_datetime", "");
            String fileUrl = json.optString("file_url", null);

            Timestamp eventTime = null;
            if (!eventDateTimeStr.isEmpty()) {
                LocalDateTime dt = LocalDateTime.parse(eventDateTimeStr);
                eventTime = Timestamp.valueOf(dt);
            }

            boolean success = new NoticeDAO().updateNotice(id, title, content, category, isEvent, eventTime, fileUrl);

            String response = success ? "{\"message\":\"Updated\"}" : "{\"error\":\"Not found\"}";
            int code = success ? 200 : 404;

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(code, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private void sendError(HttpExchange exchange, int code, String message) throws IOException {
            String error = "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(code, error.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(error.getBytes());
            }
        }
    }
}
