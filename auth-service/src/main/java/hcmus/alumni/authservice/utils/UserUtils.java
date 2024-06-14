package hcmus.alumni.authservice.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import hcmus.alumni.authservice.model.EmailActivationCodeModel;
import hcmus.alumni.authservice.model.EmailResetCodeModel;
import hcmus.alumni.authservice.repository.EmailActivationCodeRepository;
import hcmus.alumni.authservice.repository.EmailResetCodeRepository;
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

	public boolean saveResetCode(EmailResetCodeRepository emailResetCodeRepository, String email, String resetCode) {
	    Date dt = new Date();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String currentTime = sdf.format(dt);
	    
	    if (emailResetCodeRepository == null) {
	        System.err.println("EmailResetCodeRepository is null");
	        return false; 
	    }
	    
	    EmailResetCodeModel existingCode = emailResetCodeRepository.findByEmail(email);

	    if (existingCode != null) {
	        existingCode.setResetCode(resetCode);
	        existingCode.setCreateAt(currentTime); 
	        emailResetCodeRepository.save(existingCode);
	        System.out.println("Reset code updated for email: " + email);
	        return true;
	    } else {
	        EmailResetCodeModel newActivationCode = new EmailResetCodeModel();
	        newActivationCode.setEmail(email);
	        newActivationCode.setResetCode(resetCode);
	        newActivationCode.setCreateAt(currentTime);
	        emailResetCodeRepository.save(newActivationCode);
	        System.out.println("New reset code saved for email: " + email);
	        return true;
	    }
	}
	
	public boolean checkActivationCode(EmailActivationCodeRepository emailActivationCodeRepository, String email, String activationCode) {
	    EmailActivationCodeModel expectedActivateCode = emailActivationCodeRepository.findByEmail(email);
	    
	    if (expectedActivateCode == null) {
	        return false;
	    }
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date createdAt;
	    long diffInMinutes = 0; 
		try {
			createdAt = sdf.parse(expectedActivateCode.getCreateAt());
			long diffInMilliseconds = Math.abs(new Date().getTime() - createdAt.getTime());
			diffInMinutes = diffInMilliseconds / (1000 * 60);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return false;
		}
	    
	    return email.equals(expectedActivateCode.getEmail()) && activationCode.equals(expectedActivateCode.getActivationCode()) && diffInMinutes < 1;
	}

	public boolean checkResetCode(EmailResetCodeRepository emailResetCodeRepository, String email, String resetCode) {
	    EmailResetCodeModel expectedActivateCode = emailResetCodeRepository.findByEmail(email);
	    
	    if (expectedActivateCode == null) {
	        return false;
	    }
	    
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date createdAt;
	    long diffInMinutes = 0; 
		try {
			createdAt = sdf.parse(expectedActivateCode.getCreateAt());
			long diffInMilliseconds = Math.abs(new Date().getTime() - createdAt.getTime());
			diffInMinutes = diffInMilliseconds / (1000 * 60);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return false;
		}
	    
	    return email.equals(expectedActivateCode.getEmail()) && resetCode.equals(expectedActivateCode.getResetCode()) && diffInMinutes < 1;
	}

}