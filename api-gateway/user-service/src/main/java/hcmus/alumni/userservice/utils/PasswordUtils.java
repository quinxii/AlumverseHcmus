package hcmus.alumni.userservice.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {
	public static String hashPassword(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

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
