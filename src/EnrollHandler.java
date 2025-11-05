import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;

public class EnrollHandler implements HttpHandler {
    private final Enrollment enroll;

    public EnrollHandler(Enrollment enroll) {
        this.enroll = enroll;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            Main.sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        try {
            Map<String, String> fields = new java.util.HashMap<>();
            Map<String, byte[]> files = new java.util.HashMap<>();
            String boundary = Main.getBoundary(exchange);
            if (boundary != null) {
                Main.parseMultipart(exchange.getRequestBody(), boundary, fields, files);
            }

            String imagePath = fields.get("imagePath");
            if (imagePath == null || imagePath.isEmpty()) {
                Main.sendJsonResponse(exchange, 400, "{\"error\":\"imagePath required\"}");
                return;
            }

            boolean success = enroll.enroll(imagePath);
            String response = success ? "{\"message\":\"Enrolled successfully\"}" : "{\"error\":\"Enrollment failed\"}";
            Main.sendJsonResponse(exchange, success ? 200 : 400, response);
        } catch (Exception e) {
            Main.sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }
}
