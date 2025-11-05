import javax.crypto.SecretKey;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import com.sun.net.httpserver.*;

class StaticFileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        // Use absolute path based on current working directory
        File file = new File(System.getProperty("user.dir") + "/ui" + path);
        if (file.exists() && !file.isDirectory()) {
            exchange.sendResponseHeaders(200, file.length());
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } else {
            String response = "File not found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}

// --- Other Handlers (EnrollHandler, AuthenticateHandler, EncryptHandler, DecryptHandler) remain the same ---

public class Main {
    public static void main(String[] args) throws Exception {
        File authDir = new File("authorized_faces");
        if (!authDir.exists())
            authDir.mkdirs();
        double THR = 5000.0;
        Enrollment enroll = new Enrollment(authDir, THR);
        File keyFile = new File("vault.key");
        SecretKey key;
        if (keyFile.exists())
            key = KeyStoreUtil.loadKey(keyFile.toPath());
        else
            key = KeyStoreUtil.generateAES256();
        if (!keyFile.exists())
            KeyStoreUtil.saveKey(key, keyFile.toPath());
        Vault vault = new Vault(key);

        // --- Start HTTP Server for Web UI with dynamic port ---
        int port = 8081; // default for local testing
        String portEnv = System.getenv("PORT");
        if (portEnv != null) port = Integer.parseInt(portEnv);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/enroll", new EnrollHandler(enroll));
        server.createContext("/authenticate", new AuthenticateHandler(enroll));
        server.createContext("/encrypt", new EncryptHandler(vault));
        server.createContext("/decrypt", new DecryptHandler(vault));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Server started on port " + port);

        // --- Command-line interface ---
        Scanner sc = new Scanner(System.in);
        boolean unlocked = false;
        while (true) {
            System.out.println("\n1-Enroll 2-Authenticate 3-Encrypt 4-Decrypt 5-Exit > ");
            String op = sc.nextLine().trim();
            if (op.equals("1")) {
                System.out.print("Enroll image path: ");
                System.out.println(enroll.enroll(sc.nextLine().trim()) ? "Enrolled." : "Failed.");
            } else if (op.equals("2")) {
                System.out.print("Authenticate image path: ");
                unlocked = enroll.authenticate(sc.nextLine().trim());
                System.out.println(unlocked ? "Authenticated!" : "Failed.");
            } else if (op.equals("3")) {
                if (!unlocked) {
                    System.out.println("Vault locked.");
                    continue;
                }
                System.out.print("Src file: ");
                String s = sc.nextLine().trim();
                System.out.print("Dest file: ");
                String d = sc.nextLine().trim();
                try {
                    vault.encryptFile(Paths.get(s), Paths.get(d));
                    System.out.println("Encrypted.");
                } catch (Exception ex) {
                    System.out.println("Error: " + ex);
                }
            } else if (op.equals("4")) {
                System.out.print("Enc file: ");
                String s = sc.nextLine().trim();
                System.out.print("Dest file: ");
                String d = sc.nextLine().trim();
                try {
                    vault.decryptFile(Paths.get(s), Paths.get(d));
                    System.out.println("Decrypted.");
                } catch (Exception ex) {
                    System.out.println("Error: " + ex);
                }
            } else if (op.equals("5")) {
                break;
            }
        }
        sc.close();
        server.stop(0);
    }

    public static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, json.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    public static String getBoundary(HttpExchange exchange) {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.startsWith("multipart/form-data; boundary=")) {
            return contentType.substring("multipart/form-data; boundary=".length());
        }
        return null;
    }

    public static void parseMultipart(InputStream is, String boundary, Map<String, String> fields, Map<String, byte[]> files) throws IOException {
        byte[] boundaryBytes = ("--" + boundary).getBytes();
        byte[] endBoundaryBytes = ("--" + boundary + "--").getBytes();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            buffer.write(b);
        }
        byte[] data = buffer.toByteArray();
        int pos = 0;
        while (pos < data.length) {
            if (startsWith(data, pos, boundaryBytes)) {
                pos += boundaryBytes.length;
                if (pos < data.length && data[pos] == '\r') pos += 2;
                int headerEnd = indexOf(data, pos, "\r\n\r\n".getBytes());
                if (headerEnd == -1) break;
                String headers = new String(data, pos, headerEnd - pos);
                pos = headerEnd + 4;
                String name = null;
                if (headers.contains("name=\"")) {
                    int start = headers.indexOf("name=\"") + 6;
                    int end = headers.indexOf("\"", start);
                    name = headers.substring(start, end);
                }
                int contentEnd = indexOf(data, pos, boundaryBytes);
                if (contentEnd == -1) contentEnd = indexOf(data, pos, endBoundaryBytes);
                if (contentEnd == -1) break;
                byte[] content = new byte[contentEnd - pos - 2];
                System.arraycopy(data, pos, content, 0, content.length);
                if (name != null) {
                    if (headers.contains("filename=")) {
                        files.put(name, content);
                    } else {
                        fields.put(name, new String(content));
                    }
                }
                pos = contentEnd;
            } else {
                break;
            }
        }
    }

    private static boolean startsWith(byte[] data, int pos, byte[] prefix) {
        if (pos + prefix.length > data.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[pos + i] != prefix[i]) return false;
        }
        return true;
    }

    private static int indexOf(byte[] data, int pos, byte[] pattern) {
        for (int i = pos; i <= data.length - pattern.length; i++) {
            if (startsWith(data, i, pattern)) return i;
        }
        return -1;
    }
}
