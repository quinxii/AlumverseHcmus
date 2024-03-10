package hcmus.alumni.userservice.utils;

import java.security.SecureRandom;

public class AuthorizationCodeGeneratorUtils {
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8; // You can adjust the length as needed

    public static String generateAuthorizeCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        // Generate random characters from CHARACTERS string
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}
