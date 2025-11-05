import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class DecryptHandler implements HttpHandler {
    private final Vault vault;

    public DecryptHandler(Vault vault) {
        this.vault = vault;
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

            String encFile = fields.get("encFile");
            String destFile = fields.get("destFile");
            if (encFile == null || destFile == null) {
                Main.sendJsonResponse(exchange, 400, "{\"error\":\"encFile and destFile required\"}");
                return;
            }

            vault.decryptFile(Paths.get(encFile), Paths.get(destFile));
            Main.sendJsonResponse(exchange, 200, "{\"message\":\"Decrypted successfully\"}");
        } catch (Exception e) {
            Main.sendJsonResponse(exchange, 500, "{\"error\":\"Decryption failed: " + e.getMessage() + "\"}");
        }
    }
}
