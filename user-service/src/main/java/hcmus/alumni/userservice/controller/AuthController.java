package hcmus.alumni.userservice.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.userservice.dto.role.PermissionNameOnly;
import hcmus.alumni.userservice.dto.user.ResetPasswordRequestDto;
import hcmus.alumni.userservice.exception.AppException;
import hcmus.alumni.userservice.model.PasswordHistoryModel;
import hcmus.alumni.userservice.model.RoleModel;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.repository.EmailActivationCodeRepository;
import hcmus.alumni.userservice.repository.EmailResetCodeRepository;
import hcmus.alumni.userservice.repository.PasswordHistoryRepository;
import hcmus.alumni.userservice.repository.PermissionRepository;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.utils.EmailSenderUtils;
import hcmus.alumni.userservice.utils.JwtUtils;
import hcmus.alumni.userservice.utils.UserUtils;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private JwtUtils jwtUtils;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EmailActivationCodeRepository emailActivationCodeRepository;
	@Autowired
	private EmailResetCodeRepository emailResetCodeRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private PasswordHistoryRepository passwordHistoryRepository;
	@Autowired
	private PermissionRepository permissionRepository;

	private UserUtils userUtils = UserUtils.getInstance();
	private EmailSenderUtils emailSenderUtils = EmailSenderUtils.getInstance();

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String pass) {
		UserModel user = userRepository.findByEmail(email);
		boolean isMatch = passwordEncoder.matches(pass, user.getPass());

		if (isMatch) {
			if (!user.getStatusId().equals(2)) {
				throw new AppException(10100, "Tài khoản đã bị khóa hoặc xóa", HttpStatus.UNAUTHORIZED);
			}

			userRepository.setLastLogin(email, new Date());

			Set<RoleModel> roles = user.getRoles();
			List<Integer> roleIds = roles.stream().map(RoleModel::getId).collect(Collectors.toList());

			List<String> permissionNames = permissionRepository.getPermissionNamesByRoleIds(roleIds);

			Map<String, Object> response = new HashMap<>();
			response.put("jwt", jwtUtils.generateToken(user));
			response.put("permissions", permissionNames);

			return ResponseEntity.status(HttpStatus.OK).body(response);
		} else {
			throw new AppException(10102, "Email hoặc mật khẩu không hợp lệ", HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<String> signup(@RequestParam String email, @RequestParam String pass) {
		UserModel newUser = new UserModel(email, passwordEncoder.encode(pass));
		newUser.setStatusId(2);

		try {
			userRepository.save(newUser);
		} catch (IllegalArgumentException e) {
			throw new AppException(10200, "Email hoặc mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(10201, "Email đã tồn tại", HttpStatus.CONFLICT);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PostMapping("/send-authorize-code")
	public ResponseEntity<String> sendAuthorizeCode(@RequestParam String email) {
		UserModel existingUser = userRepository.findByEmail(email);
		if (existingUser != null) {
			throw new AppException(10300, "Email đã tồn tại", HttpStatus.CONFLICT);
		}

		if (email == null || email.isEmpty()) {
			throw new AppException(10301, "Email không được để trống", HttpStatus.BAD_REQUEST);
		}

		try {
			emailSenderUtils.sendEmail(emailActivationCodeRepository, email);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(10302, "Gửi mã xác thực thất bại", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/verify-authorize-code")
	public ResponseEntity<String> verifyAuthorizeCode(@RequestParam String email, @RequestParam String activationCode) {
		if (email == null || email.isEmpty()) {
			throw new AppException(10400, "Email không được để trống", HttpStatus.BAD_REQUEST);
		}

		if (activationCode == null || activationCode.isEmpty()) {
			throw new AppException(10401, "Mã xác thực không được để trống", HttpStatus.BAD_REQUEST);
		}

		boolean isValid = userUtils.checkActivationCode(emailActivationCodeRepository, email, activationCode);
		if (!isValid) {
			throw new AppException(10402, "Mã xác thực không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto req) {
		UserModel user = userRepository.findByEmail(req.getEmail());
		if (user == null) {
			throw new AppException(10500, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
		}

		if (!passwordEncoder.matches(req.getOldPassword(), user.getPass())) {
			throw new AppException(10501, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST);
		}

		user.setPass(passwordEncoder.encode(req.getNewPassword()));
		user.setUpdateAt(new Date());

		PasswordHistoryModel currentPasswordHistory = passwordHistoryRepository.findByUserId(user.getId());

		if (currentPasswordHistory != null) {
			currentPasswordHistory.setPassword(user.getPass());
			currentPasswordHistory.setAutoGenerated(false);
			passwordHistoryRepository.save(currentPasswordHistory);
		} else {
			PasswordHistoryModel passwordHistory = new PasswordHistoryModel(user.getId(), user.getPass(), false,
					new Date());
			passwordHistoryRepository.save(passwordHistory);
		}

		userRepository.save(user);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestParam String email) {
		if (email == null || email.isEmpty()) {
			throw new AppException(10600, "Email không được để trống", HttpStatus.BAD_REQUEST);
		}

		UserModel user = userRepository.findByEmail(email);
		if (user == null) {
			throw new AppException(10601, "Email không tồn tại", HttpStatus.BAD_REQUEST);
		}

		try {
			emailSenderUtils.sendPasswordResetEmail(emailResetCodeRepository, email);
			return ResponseEntity.status(HttpStatus.OK).body("");
		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(10602, "Gửi mã xác thực thất bại", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/verify-reset-code")
	public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String resetCode,
			@RequestParam String newPassword) {
		if (email == null || email.isEmpty()) {
			throw new AppException(10700, "Email không được để trống", HttpStatus.BAD_REQUEST);
		}

		if (resetCode == null || resetCode.isEmpty()) {
			throw new AppException(10701, "Mã xác thực không được để trống", HttpStatus.BAD_REQUEST);
		}

		if (newPassword == null || newPassword.isEmpty()) {
			throw new AppException(10702, "Mật khẩu mới không được để trống", HttpStatus.BAD_REQUEST);

		}

		UserModel user = userRepository.findByEmail(email);
		if (user == null) {
			throw new AppException(10703, "Email không tồn tại", HttpStatus.BAD_REQUEST);
		}

		boolean isValid = userUtils.checkResetCode(emailResetCodeRepository, email, resetCode);
		if (!isValid) {
			throw new AppException(10704, "Mã xác thực không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST);
		}

		user.setPass(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}
}