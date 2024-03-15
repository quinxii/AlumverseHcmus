package hcmus.alumni.userservice.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.repository.VerifyAlumniRepository;
import hcmus.alumni.userservice.utils.EmailSenderUtils;
import hcmus.alumni.userservice.utils.GCPConnectionUtils;
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
	private ImageUtils imageUtils;

	@PostMapping("/login")
	public UserModel login(@RequestParam String email, @RequestParam String pass) {
		UserModel user = new UserModel();

		// Find user by email
		UserModel foundUser = userRepository.findByEmailAndPass(email, PasswordUtils.hashPassword(pass));
		if (foundUser != null) {
			user = foundUser;
			user.setLastLogin(new Date());
			userRepository.save(user);
		}
		return user;
	}

	@PostMapping("/sendAuthorizeCode")
	public Boolean sendAuthorizeCode(@RequestParam String id) {
		String email = getUserEmail(id);

		if (email == null) {
			return false;
		}

		try {
			EmailSenderUtils.sendEmail(email);
			return true;
		} catch (MailException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getUserEmail(String userId) {
		UserModel user = null;
		UserModel foundUser = userRepository.findUserById(userId);

		if (foundUser != null) {
			user = foundUser;
		}

		return user != null ? user.getEmail() : null;
	}

	/*
	 * @PostMapping("/verifyAuthorizeCode") public Boolean
	 * verifyAuthorizeCode(@RequestParam String authorizeCode) { UserModel user =
	 * new UserModel();
	 * 
	 * // Find user by email UserModel foundUser =
	 * userRepository.findByEmailAndPass(email,PasswordUtils.hashPassword(pass)); if
	 * (foundUser != null) { user = foundUser; user.setLastLogin(new Date());
	 * userRepository.save(user); } return false; }
	 * 
	 * @PostMapping("/signup") public UserModel signup(@RequestParam String
	 * email, @RequestParam String pass, @RequestParam String fullName,
	 * 
	 * @RequestParam String studentId, @RequestParam String beginningYear) { //
	 * Check if the user already exists if
	 * (userRepository.findByEmail(newUser.getEmail()) != null) { // User already
	 * exists return null; }
	 * 
	 * // Hash the password before saving it String hashedPassword =
	 * PasswordUtils.hashPassword(newUser.getPass());
	 * newUser.setPass(hashedPassword);
	 * 
	 * // Set the creation date newUser.setCreateAt(new Date());
	 * 
	 * // Save the new user return userRepository.save(newUser); }
	 */
	
	@GetMapping("/alumni-verification")
	public List<VerifyAlumniModel> getAllAlumniVerification() {
	    return verifyAlumniRepository.findAllByIsDeleteEquals(false);
	}
	
	@GetMapping("/alumni-verification/{user_id}")
	public ResponseEntity<VerifyAlumniModel> getAlumniVerificationByUserId(@PathVariable String user_id) {
	    Optional<VerifyAlumniModel> alumniVerification = verifyAlumniRepository.findByUserIdAndIsDeleteEquals(user_id, false);
	    
	    return alumniVerification.map(response -> ResponseEntity.ok().body(response))
	            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
