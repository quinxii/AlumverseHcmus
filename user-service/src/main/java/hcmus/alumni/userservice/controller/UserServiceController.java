package hcmus.alumni.userservice.controller;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.userservice.common.Privacy;
import hcmus.alumni.userservice.common.UserUtils;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.repository.EmailActivationCodeRepository;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.repository.VerifyAlumniRepository;
import hcmus.alumni.userservice.utils.EmailSenderUtils;
import hcmus.alumni.userservice.utils.ImageUtils;
import hcmus.alumni.userservice.utils.PasswordUtils;

@RestController
@CrossOrigin(origins = "http://localhost:3000") // Allow requests from Web
@RequestMapping("/user")
public class UserServiceController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private VerifyAlumniRepository verifyAlumniRepository;
	
	@Autowired
    private EmailActivationCodeRepository emailActivationCodeRepository;
	
	@Autowired
	private ImageUtils imageUtils;
	
	private UserUtils userUtils = UserUtils.getInstance();
	private PasswordUtils passwordUtils = PasswordUtils.getInstance();
	private EmailSenderUtils emailSenderUtils = EmailSenderUtils.getInstance();

	@PostMapping("/login")
	public UserModel login(@RequestParam String email, @RequestParam String pass) {
		UserModel user = new UserModel();

		// Find user by email
		UserModel foundUser = userRepository.findByEmailAndPass(email, passwordUtils.hashPassword(pass));
		if (foundUser != null) {
			user = foundUser;
			user.setLastLogin(new Date());
			userRepository.save(user);
		}
		return user;
	}

	@PostMapping("/send-authorize-code")
	public String sendAuthorizeCode(@RequestParam String email) {
	    if (email == null || email.isEmpty()) {
	        return "Send Authorize code failed: Email is required";
	    }

	    try {
	        emailSenderUtils.sendEmail(emailActivationCodeRepository, email);
	        return "Send Authorize code success";
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Send Authorize code failed: " + e.getMessage();
	    }
	}

	@PostMapping("/verify-authorize-code")
	public String verifyAuthorizeCode(@RequestParam String email, @RequestParam String activationCode) {
	    if (email == null || email.isEmpty() || activationCode == null || activationCode.isEmpty()) {
	        return "Verify Authorize code failed: Email and activation code are required";
	    }

	    try {
	        boolean isValid = userUtils.checkActivationCode(emailActivationCodeRepository, email, activationCode);
	        if (isValid) {
	            return "Verify Authorize code success: Activation code is valid";
	        } else {
	            return "Verify Authorize code failed: Activation code is invalid or expired";
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Verify Authorize code failed: " + e.getMessage();
	    }
	}

	@PostMapping("/signup")
	public String signup(@RequestParam String email,
	                     @RequestParam String pass) {

	    UserModel existingUser = userRepository.findByEmail(email);
	    if (existingUser != null) {
	        return "User with this email already exists";
	    }

	    UserModel newUser = new UserModel();
	    
	    // Generate UUID
	    UUID uuid = UUID.randomUUID();
	    String uuidString = uuid.toString(); 
	    
	    if(uuidString.length() != 36) {
	        return "Error occurred during user signup";
	    }
	    newUser.setId(uuidString);
	    newUser.setEmail(email);

	    String hashedPassword = passwordUtils.hashPassword(pass);
	    newUser.setPass(hashedPassword);
	    
	    newUser.setRoleId("8ea1665e-74b4-43ac-a966-bf10e938da42");
	    newUser.setSexId("8ea1665e-74b4-43ac-a966-bf10e938da43");
	    newUser.setStatusId("8ea1665e-74b4-43ac-a966-bf10e938da45");
	    newUser.setEmailPrivacy(Privacy.PUBLIC);
	    newUser.setPhonePrivacy(Privacy.PUBLIC);
	    newUser.setSexPrivacy(Privacy.PUBLIC);
	    newUser.setDobPrivacy(Privacy.PUBLIC);

	    userRepository.save(newUser);

	    return "Signup successful";
	}

	@PostMapping("/alumni-verification")
	public ResponseEntity<String> createAlumniVerification(
			@RequestParam(value = "avatar", required = false) MultipartFile avatar,
			@RequestParam("full_name") String full_name,
			@RequestParam(value = "student_id", required = false) String student_id,
			@RequestParam(value = "beginning_year", required = false) Integer beginning_year,
			@RequestParam(value = "social_media_link", required = false) String social_media_link) {
		String userID = "8ea1665e-74b4-43ac-a966-bf10e938da44"; // delete after implementing jwt
		VerifyAlumniModel verifyAlumni = new VerifyAlumniModel(userID, student_id, beginning_year, social_media_link);

		try {
			userRepository.setFullName(userID, full_name);
			if (student_id != null || beginning_year != null || social_media_link != null) {
				verifyAlumniRepository.save(verifyAlumni);
			}
			String avatarUrl = imageUtils.saveImageToStorage(imageUtils.getAvatarPath(), avatar, userID);
			userRepository.setAvatarUrl(userID, avatarUrl);
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("Post alumni verification successfully");
	}
}
