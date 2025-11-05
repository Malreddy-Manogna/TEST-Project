import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
public class Vault {
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12, TAG_BITS = 128;
    private final SecretKey key; private final SecureRandom rnd = new SecureRandom();
    public Vault(SecretKey k){
    	this.key=k;
    }
    public void encryptFile(Path in, Path out) throws Exception {
        byte[] iv=new byte[IV_LEN]; rnd.nextBytes(iv);
        Cipher c=Cipher.getInstance(TRANSFORM);
        c.init(Cipher.ENCRYPT_MODE,key,new GCMParameterSpec(TAG_BITS,iv));
        byte[] pt=Files.readAllBytes(in), ct=c.doFinal(pt);
        try(OutputStream os=Files.newOutputStream(out)){os.write(iv);os.write(ct);}
    }
    public void decryptFile(Path in, Path out)throws Exception{
        byte[] d=Files.readAllBytes(in);
        System.out.println("Decrypting file: " + in + ", size: " + d.length);
        if (d.length < IV_LEN) {
            throw new Exception("File too small to be encrypted with this method");
        }
        byte[] iv=new byte[IV_LEN]; System.arraycopy(d,0,iv,0,IV_LEN);
        byte[] ct=new byte[d.length-IV_LEN]; System.arraycopy(d,IV_LEN,ct,0,ct.length);
        Cipher c=Cipher.getInstance(TRANSFORM);
        c.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(TAG_BITS,iv));
        byte[] pt=c.doFinal(ct);
        Files.createDirectories(out.getParent());
        Files.write(out,pt);
        System.out.println("Decryption successful for: " + out);
    }
}
