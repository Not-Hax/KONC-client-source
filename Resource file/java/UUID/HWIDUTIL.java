package meow.konc.hack.gui.font.ouo;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HWIDUTIL {

    public static String getHWID() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        StringBuilder s = new StringBuilder();
        String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
        byte[] bytes = main.getBytes("UTF-8");
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] nd5 = messageDigest.digest(bytes);
        int i = 0;
        for (byte b : nd5) {
            s.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
            if (i != nd5.length) {
                s.append("-");
            }
            i++;
        }
        return s.toString();
    }
}
