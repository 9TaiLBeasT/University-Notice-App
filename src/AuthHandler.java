import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class AuthHandler implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            if (path.endsWith("/signup")) {
                handleSignup(exchange);
            } else if (path.endsWith("/login")) {
                handleLogin(exchange);
            } else {
                sendResponse(exchange, 404, "Endpoint not found");
            }
        } else {
            sendResponse(exchange, 405, "Only POST method allowed");
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        JSONObject body = parseRequestBody(exchange);
        if (body == null) {
            sendResponse(exchange, 400, "Invalid JSON");
            return;
        }

        String name = body.optString("name");
        String email = body.optString("email");
        String password = body.optString("password");
        String role = body.optString("role"); // student or faculty
        String regNumber = body.optString("reg_number", null);
        String department = body.optString("department");

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            sendResponse(exchange, 400, "Missing required fields");
            return;
        }

        if (!email.endsWith("@aurora.edu.in")) {
            sendResponse(exchange, 403, "Only aurora.edu.in emails allowed");
            return;
        }

        if (userDAO.findByEmail(email).isPresent()) {
            sendResponse(exchange, 409, "Email already exists");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(name, email, hashedPassword, role, regNumber, department);

        boolean success = userDAO.insertUser(user);
        if (success) {
            sendResponse(exchange, 200, "Signup successful");
        } else {
            sendResponse(exchange, 500, "Signup failed");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        JSONObject body = parseRequestBody(exchange);
        if (body == null) {
            sendResponse(exchange, 400, "Invalid JSON");
            return;
        }

        String email = body.optString("email");
        String password = body.optString("password");

        if (email.isEmpty() || password.isEmpty()) {
            sendResponse(exchange, 400, "Email and password required");
            return;
        }

        Optional<User> optionalUser = userDAO.findByEmail(email);
        if (optionalUser.isEmpty()) {
            sendResponse(exchange, 401, "Invalid credentials");
            return;
        }

        User user = optionalUser.get();
        if (BCrypt.checkpw(password, user.getPassword())) {
            JSONObject res = new JSONObject();
            res.put("message", "Login successful");
            res.put("name", user.getName());
            res.put("email", user.getEmail());
            res.put("role", user.getRole());
            res.put("department", user.getDepartment());
            res.put("reg_number", user.getRegNumber());

            sendJSON(exchange, 200, res);
        } else {
            sendResponse(exchange, 401, "Invalid credentials");
        }
    }

    private JSONObject parseRequestBody(HttpExchange exchange) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder bodyBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bodyBuilder.append(line);
            }
            return new JSONObject(bodyBuilder.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject res = new JSONObject();
        res.put("message", message);
        sendJSON(exchange, statusCode, res);
    }

    private void sendJSON(HttpExchange exchange, int statusCode, JSONObject json) throws IOException {
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
