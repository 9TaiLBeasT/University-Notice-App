import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class NoticeHttpServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/notices", new NoticeHandler());
        server.setExecutor(null); // default executor
        System.out.println("Server started on port 8000");
        server.start();
    }

    static class NoticeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

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
                List<Notice> notices = NoticeDAO.getAllNotices();

                JSONArray jsonArray = new JSONArray();
                for (Notice notice : notices) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", notice.getId());
                    obj.put("title", notice.getTitle());
                    obj.put("content", notice.getContent());
                    obj.put("category", notice.getCategory());
                    obj.put("created_at", notice.getCreatedAt());
                    jsonArray.put(obj);
                }

                String response = jsonArray.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
                String error = "{\"error\":\"Unable to fetch notices\"}";
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

                JSONObject json = new JSONObject(requestBody.toString());
                String title = json.getString("title");
                String content = json.getString("content");
                String category = json.has("category") ? json.getString("category") : "General";

                // Create and save notice
                Notice notice = new Notice(title, content, category);
                NoticeDAO noticeDAO = new NoticeDAO();
                noticeDAO.addNotice(notice);

                // Send push notification
                FCMSender.sendPushNotification(title, content);

                String response = "{\"message\":\"Notice added and push notification sent\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
                String error = "{\"error\":\"Failed to add notice\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(error.getBytes());
                }
            }
        }
    }
}
