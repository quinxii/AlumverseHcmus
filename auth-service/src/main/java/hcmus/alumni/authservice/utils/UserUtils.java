package hcmus.alumni.authservice.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import hcmus.alumni.authservice.model.EmailActivationCodeModel;
import hcmus.alumni.authservice.repository.EmailActivationCodeRepository;
import hcmus.alumni.authservice.repository.UserRepository;

public class UserUtils {
	private static volatile UserUtils instance  = null;
	
	@Autowired
	private UserRepository userRepository;
	
	private UserUtils() {
        super();
    }
	
	public static UserUtils getInstance() {
        if (instance == null) {
            synchronized (UserUtils.class) {
                if (instance == null) {
                    instance = new UserUtils();
                }
            }
        }
        return instance;
    }
	
	public boolean saveActivationCode(EmailActivationCodeRepository emailActivationCodeRepository, String email, String activationCode) {
	    Date dt = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String currentTime = sdf.format(dt);
	    
	    if (emailActivationCodeRepository == null) {
	        System.err.println("EmailActivationCodeRepository is null");
	        return false; 
	    }
	    
	    EmailActivationCodeModel existingCode = emailActivationCodeRepository.findByEmail(email);

	    if (existingCode != null) {
	        // Update the existing email activation code
	        existingCode.setActivationCode(activationCode);
	        existingCode.setCreateAt(currentTime); 
	        emailActivationCodeRepository.save(existingCode);
	        System.out.println("Activation code updated for email: " + email);
	        return true;
	    } else {
	        // Create a new email activation code
	        EmailActivationCodeModel newActivationCode = new EmailActivationCodeModel();
	        newActivationCode.setEmail(email);
	        newActivationCode.setActivationCode(activationCode);
	        newActivationCode.setCreateAt(currentTime);
	        emailActivationCodeRepository.save(newActivationCode);
	        System.out.println("New activation code saved for email: " + email);
	        return true;
	    }
	}
	
	public boolean checkActivationCode(EmailActivationCodeRepository emailActivationCodeRepository, String email, String activationCode) throws Exception {
	    EmailActivationCodeModel expectedActivateCode = emailActivationCodeRepository.findByEmail(email);
	    
	    if (expectedActivateCode == null) {
	        return false;
	    }
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date createdAt = sdf.parse(expectedActivateCode.getCreateAt());
	    
	    long diffInMilliseconds = Math.abs(new Date().getTime() - createdAt.getTime());
	    long diffInMinutes = diffInMilliseconds / (1000 * 60);

	    return email.equals(expectedActivateCode.getEmail()) && activationCode.equals(expectedActivateCode.getActivationCode()) && diffInMinutes < 15;
	}

}