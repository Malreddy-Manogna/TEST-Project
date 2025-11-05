import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class EncryptHandler implements HttpHandler {
    private final Vault vault;

    public EncryptHandler(Vault vault) {
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

            String srcFile = fields.get("srcFile");
            String destFile = fields.get("destFile");
            if (srcFile == null || destFile == null) {
                Main.sendJsonResponse(exchange, 400, "{\"error\":\"srcFile and destFile required\"}");
                return;
            }

            vault.encryptFile(Paths.get(srcFile), Paths.get(destFile));
            Main.sendJsonResponse(exchange, 200, "{\"message\":\"Encrypted successfully\"}");
        } catch (Exception e) {
            Main.sendJsonResponse(exchange, 500, "{\"error\":\"Encryption failed: " + e.getMessage() + "\"}");
        }
    }
}
