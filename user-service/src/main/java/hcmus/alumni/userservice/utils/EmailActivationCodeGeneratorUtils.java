package hcmus.alumni.userservice.utils;

import java.security.SecureRandom;

public class EmailActivationCodeGeneratorUtils {
	
	private static final String CHARACTERS = "0123456789";
    private static final int CODE_LENGTH = 4; 

    private static volatile EmailActivationCodeGeneratorUtils instance  = null;
    
    private EmailActivationCodeGeneratorUtils() {
        super();
    }
	
	public static EmailActivationCodeGeneratorUtils getInstance() {
        if (instance == null) {
            synchronized (EmailActivationCodeGeneratorUtils.class) {
                if (instance == null) {
                    instance = new EmailActivationCodeGeneratorUtils();
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