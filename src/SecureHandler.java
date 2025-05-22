import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;

public class SecureHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendError(exchange, 405, "Only GET allowed");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        String role = "general"; // default fallback
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("role=")) {
                    role = param.split("=")[1].toLowerCase();
                    break;
                }
            }
        }

        List<Notice> filteredNotices = NoticeDAO.getNoticesForRole(role);
        JSONArray responseJson = new JSONArray();
        for (Notice n : filteredNotices) {
            JSONObject obj = new JSONObject();
            obj.put("id", n.getId());
            obj.put("title", n.getTitle());
            obj.put("content", n.getContent());
            obj.put("category", n.getCategory());
            obj.put("created_at", n.getCreatedAt());
            obj.put("is_event", n.isEvent());
            obj.put("event_datetime", n.getEventTime() != null ? n.getEventTime().toString() : "");
            responseJson.put(obj);
        }

        byte[] responseBytes = responseJson.toString().getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
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
