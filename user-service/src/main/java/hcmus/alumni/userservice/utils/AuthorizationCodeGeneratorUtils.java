package hcmus.alumni.userservice.utils;

import java.security.SecureRandom;

public class AuthorizationCodeGeneratorUtils {
	
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 8; 

    private static volatile AuthorizationCodeGeneratorUtils instance  = null;
    
    private AuthorizationCodeGeneratorUtils() {
        super();
    }
	
	public static AuthorizationCodeGeneratorUtils getInstance() {
        if (instance == null) {
            synchronized (AuthorizationCodeGeneratorUtils.class) {
                if (instance == null) {
                    instance = new AuthorizationCodeGeneratorUtils();
                }
            }
        }
        return instance;
    }
    
    public String generateAuthorizeCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }

        return sb.toString();
    }
}
