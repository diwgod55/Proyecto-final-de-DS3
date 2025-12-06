package mx.unison.servidor.com;
import javax.crypto.Cipher; import javax.crypto.spec.SecretKeySpec; import java.util.Base64;
public class encriptacion {
    private static final String KEY = "0123456789abcdef";
    public static String encrypt(String plain){
        try{ SecretKeySpec sk = new SecretKeySpec(KEY.getBytes(), "AES"); Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.ENCRYPT_MODE, sk); return Base64.getEncoder().encodeToString(c.doFinal(plain.getBytes())); }catch(Exception ex){ throw new RuntimeException(ex); }
    }
    public static String decrypt(String b64){
        try{ SecretKeySpec sk = new SecretKeySpec(KEY.getBytes(), "AES"); Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding"); c.init(Cipher.DECRYPT_MODE, sk); return new String(c.doFinal(Base64.getDecoder().decode(b64))); }catch(Exception ex){ throw new RuntimeException(ex); }
    }
}
