package hcmus.alumni.userservice.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.RequestBody;
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
	public ResponseEntity<Map<String, Object>> getAlumniVerificationByUserId(@PathVariable String user_id) {
		Optional<VerifyAlumniModel> alumniVerificationOptional = verifyAlumniRepository.findByUserIdAndIsDeleteEquals(user_id, false);

		if (alumniVerificationOptional.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
	        errorResponse.put("error", "No alumni verification with that userid");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
		}
	    
		VerifyAlumniModel alumniVerification = alumniVerificationOptional.get();

		Map<String, Object> response = new HashMap<>();
		response.put("id", alumniVerification.getId());
		response.put("userId", alumniVerification.getUserId());
		response.put("studentId", alumniVerification.getStudentId());
		response.put("beginningYear", alumniVerification.getBeginningYear());
		response.put("socialMediaLink", alumniVerification.getSocialMediaLink());
		response.put("comment", alumniVerification.getComment());
		response.put("status", alumniVerification.getStatus());
		response.put("createdAt", alumniVerification.getCreatedAt());
		response.put("isDelete", alumniVerification.getIsDelete());
		response.put("fullName", userRepository.findFullNameByUserId(user_id));
		response.put("avatarUrl", userRepository.findAvatarUrlByUserId(user_id));
        
		return ResponseEntity.ok(response);
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
	
	@PutMapping("/alumni-verification/{user_id}")
	public ResponseEntity<String> updateAlumniVerification(
	        @PathVariable String user_id,
	        @RequestBody Map<String, Object> requestBody) {

		Optional<VerifyAlumniModel> optionalAlumni = verifyAlumniRepository.findByUserIdAndIsDeleteEquals(user_id, false);

	    if (optionalAlumni.isEmpty()) {
	        return ResponseEntity.notFound().build();
	    }

	    VerifyAlumniModel alumni = optionalAlumni.get();

	    try {
	        if (requestBody.containsKey("full_name")) {
	            String full_name = (String) requestBody.get("full_name");
	            userRepository.setFullName(user_id, full_name);
	        }
	        if (requestBody.containsKey("student_id")) {
	            String student_id = (String) requestBody.get("student_id");
	            alumni.setStudentId(student_id);
	        }
	        if (requestBody.containsKey("beginning_year")) {
	            Integer beginning_year = (Integer) requestBody.get("beginning_year");
	            alumni.setBeginningYear(beginning_year);
	        }
	        if (requestBody.containsKey("social_media_link")) {
	            String social_media_link = (String) requestBody.get("social_media_link");
	            alumni.setSocialMediaLink(social_media_link);
	        }
	        if (requestBody.containsKey("avatar")) {
	            MultipartFile avatar = (MultipartFile) requestBody.get("avatar");
	            String avatarUrl = imageUtils.saveImageToStorage(imageUtils.getAvatarPath(), avatar, user_id);
	            userRepository.setAvatarUrl(user_id, avatarUrl);
	        }

	        verifyAlumniRepository.save(alumni);
	        return ResponseEntity.ok("Alumni verification updated successfully");
	    } catch (IllegalArgumentException | IOException e) {
	        e.printStackTrace(); // Log the exception
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update alumni verification");
	    }
	}
}
