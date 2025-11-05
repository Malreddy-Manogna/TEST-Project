import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.*;
import java.util.Base64;
public class KeyStoreUtil {
    public static SecretKey generateAES256() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES"); 
        kg.init(256); 
        return kg.generateKey();
    }
    public static void saveKey(SecretKey k, Path p) throws Exception {
        Files.write(p, Base64.getEncoder().encode(k.getEncoded()));
    }
    public static SecretKey loadKey(Path p) throws Exception {
        byte[] raw = Base64.getDecoder().decode(Files.readAllBytes(p));
        return new javax.crypto.spec.SecretKeySpec(raw, "AES");
    }
}

