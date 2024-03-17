package hcmus.alumni.userservice.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
	private static volatile PasswordUtils instance  = null;
	
	private PasswordUtils() {
        super();
    }
	
	public static PasswordUtils getInstance() {
        if (instance == null) {
            synchronized (PasswordUtils.class) {
                if (instance == null) {
                    instance = new PasswordUtils();
                }
            }
        }
        return instance;
    }
	
	public String hashPassword(String pass) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(pass.getBytes(StandardCharsets.UTF_8));

			StringBuilder sb = new StringBuilder();
			for (byte hashByte : hashBytes) {
				sb.append(String.format("%02x", hashByte));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
}
