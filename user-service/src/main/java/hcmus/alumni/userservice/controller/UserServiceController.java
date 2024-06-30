package hcmus.alumni.userservice.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.userservice.common.Privacy;
import hcmus.alumni.userservice.config.UserConfig;
import hcmus.alumni.userservice.dto.AchievementDto;
import hcmus.alumni.userservice.dto.EducationDto;
import hcmus.alumni.userservice.dto.IAchievementDto;
import hcmus.alumni.userservice.dto.IEducationDto;
import hcmus.alumni.userservice.dto.IJobDto;
import hcmus.alumni.userservice.dto.JobDto;
import hcmus.alumni.userservice.dto.UserSearchDto;
import hcmus.alumni.userservice.dto.VerifyAlumniDto;
import hcmus.alumni.userservice.exception.AppException;
import hcmus.alumni.userservice.model.AchievementModel;
import hcmus.alumni.userservice.model.AlumniModel;
import hcmus.alumni.userservice.model.EducationModel;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.JobModel;
import hcmus.alumni.userservice.model.PasswordHistoryModel;
import hcmus.alumni.userservice.model.RoleModel;
import hcmus.alumni.userservice.model.SexModel;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.repository.AchievementRepository;
import hcmus.alumni.userservice.repository.AlumniRepository;
import hcmus.alumni.userservice.repository.EducationRepository;
import hcmus.alumni.userservice.repository.JobRepository;
import hcmus.alumni.userservice.repository.PasswordHistoryRepository;
import hcmus.alumni.userservice.repository.RoleRepository;
import hcmus.alumni.userservice.repository.StatusUserGroupRepository;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.repository.VerifyAlumniRepository;
import hcmus.alumni.userservice.utils.EmailSenderUtils;
import hcmus.alumni.userservice.utils.ImageUtils;

@RestController
@RequestMapping("/user")
public class UserServiceController {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private VerifyAlumniRepository verifyAlumniRepository;

	@Autowired
	private StatusUserGroupRepository statusUserGroupRepository;

	@Autowired
	private ImageUtils imageUtils;

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private PasswordHistoryRepository passwordHistoryRepository;

	@Autowired
	private AlumniRepository alumniRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private EducationRepository educationRepository;

	@Autowired
	private AchievementRepository achievementRepository;

	private EmailSenderUtils emailSenderUtils = EmailSenderUtils.getInstance();

	private final static int MAXIMUM_PAGES = 50;

	@PreAuthorize("hasAnyAuthority('AlumniVerify.Read')")
	@GetMapping("/alumni-verification/count")
	public ResponseEntity<Long> getPendingAlumniVerificationCount(@RequestParam String status) {
		switch (status) {
		case "pending":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusEquals(false, VerifyAlumniModel.Status.PENDING));
		case "resolved":
			return ResponseEntity.status(HttpStatus.OK).body(
					verifyAlumniRepository.countByIsDeleteEqualsAndStatusNot(false, VerifyAlumniModel.Status.PENDING));
		case "approved":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusEquals(false, VerifyAlumniModel.Status.APPROVED));
		case "denied":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusEquals(false, VerifyAlumniModel.Status.DENIED));
		default:
			throw new AppException(20100, "status không hợp lệ", HttpStatus.BAD_REQUEST);
		}
	}

	@PreAuthorize("hasAnyAuthority('AlumniVerify.Read')")
	@GetMapping("/alumni-verification")
	public ResponseEntity<HashMap<String, Object>> searchAlumniVerification(@RequestParam String status,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "criteria", required = false, defaultValue = "email") String criteria,
			@RequestParam(value = "createAtOrder", required = false, defaultValue = "desc") String createAtOrder,
			@RequestParam(value = "studentIdOrder", required = false, defaultValue = "") String studentIdOrder,
			@RequestParam(value = "fullNameOrder", required = false, defaultValue = "") String fullNameOrder,
			@RequestParam(value = "beginningYearOrder", required = false, defaultValue = "") String beginningYearOrder,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") String facultyId) {

		// Initiate
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VerifyAlumniDto> cq = cb.createQuery(VerifyAlumniDto.class);
		// From
		Root<VerifyAlumniModel> root = cq.from(VerifyAlumniModel.class);

		// Join
		Join<VerifyAlumniModel, UserModel> userJoin = root.join("user", JoinType.INNER);
		Join<VerifyAlumniModel, FacultyModel> facultyJoin = root.join("faculty", JoinType.LEFT);

		// Select
		Selection<String> idSelection = root.get("id");
		Selection<String> studentIdSelection = root.get("studentId");
		Selection<Integer> beginningYearSelection = root.get("beginningYear");
		Selection<String> socialMediaLinkSelection = root.get("socialMediaLink");
		Selection<String> commentSelection = root.get("comment");
		Selection<VerifyAlumniModel.Status> statusSelection = root.get("status");
		Selection<String> emailSelection = userJoin.get("email");
		Selection<String> fullNameSelection = userJoin.get("fullName");
		Selection<String> avatarUrlSelection = userJoin.get("avatarUrl");
		Selection<String> facultyNameSelection = facultyJoin.get("name");

		cq.multiselect(idSelection, studentIdSelection, beginningYearSelection, socialMediaLinkSelection,
				commentSelection, statusSelection, emailSelection, fullNameSelection, avatarUrlSelection,
				facultyNameSelection);

		// Where
		Predicate statusPredication = null;
		switch (status) {
		case "pending":
			statusPredication = cb.equal(root.get("status"), VerifyAlumniModel.Status.PENDING);
			break;
		case "resolved":
			statusPredication = cb.notEqual(root.get("status"), VerifyAlumniModel.Status.PENDING);
			break;
		case "approved":
			statusPredication = cb.equal(root.get("status"), VerifyAlumniModel.Status.APPROVED);
			break;
		case "denied":
			statusPredication = cb.equal(root.get("status"), VerifyAlumniModel.Status.DENIED);
			break;
		default:
			throw new AppException(20200, "status không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		Predicate isDeletePredicate = cb.equal(root.get("isDelete"), false);
		Predicate criteriaPredicate = null;
		if (criteria.equals("email") || criteria.equals("fullName")) {
			criteriaPredicate = cb.like(userJoin.get(criteria), "%" + keyword + "%");
		} else if (criteria.equals("studentId") || criteria.equals("beginningYear")) {
			criteriaPredicate = cb.like(root.get(criteria).as(String.class), "%" + keyword + "%");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		Predicate facultyIdPredicate = null;
		if (!facultyId.equals("0")) {
			facultyIdPredicate = cb.equal(facultyJoin.get("id"), facultyId);
			cq.where(statusPredication, criteriaPredicate, isDeletePredicate, facultyIdPredicate);
		} else {
			cq.where(statusPredication, criteriaPredicate, isDeletePredicate);
		}

		List<Order> orderList = new ArrayList<Order>();
		if (studentIdOrder.equals("asc")) {
			orderList.add(cb.asc(root.get("studentId")));
		} else if (studentIdOrder.equals("desc")) {
			orderList.add(cb.desc(root.get("studentId")));
		}
		if (fullNameOrder.equals("asc")) {
			orderList.add(cb.asc(userJoin.get("fullName")));
		} else if (fullNameOrder.equals("desc")) {
			orderList.add(cb.desc(userJoin.get("fullName")));
		}
		if (beginningYearOrder.equals("asc")) {
			orderList.add(cb.asc(root.get("beginningYear")));
		} else if (beginningYearOrder.equals("desc")) {
			orderList.add(cb.desc(root.get("beginningYear")));
		}
		if (createAtOrder.equals("asc")) {
			orderList.add(cb.asc(root.get("createAt")));
		} else if (createAtOrder.equals("desc")) {
			orderList.add(cb.desc(root.get("createAt")));
		}
		cq.orderBy(orderList);

		HashMap<String, Object> result = new HashMap<String, Object>();
		TypedQuery<VerifyAlumniDto> typedQuery = em.createQuery(cq);
		result.put("itemNumber", typedQuery.getResultList().size());
		typedQuery.setFirstResult(offset);
		typedQuery.setMaxResults(limit);
		result.put("items", typedQuery.getResultList());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('AlumniVerify.Read')")
	@GetMapping("/alumni-verification/{user_id}")
	public ResponseEntity<Map<String, Object>> getAlumniVerificationByUserId(@PathVariable String user_id) {
		Optional<VerifyAlumniModel> alumniVerificationOptional = verifyAlumniRepository
				.findByUserIdAndIsDeleteEquals(user_id, false);

		if (alumniVerificationOptional.isEmpty()) {
			throw new AppException(20300, "Không tìm thấy thông tin xác thực cựu sinh viên", HttpStatus.NOT_FOUND);
		}

		VerifyAlumniModel alumniVerification = alumniVerificationOptional.get();

		Map<String, Object> response = new HashMap<>();
		response.put("id", alumniVerification.getId());
		response.put("userId", alumniVerification.getUser().getId());
		response.put("studentId", alumniVerification.getStudentId());
		response.put("beginningYear", alumniVerification.getBeginningYear());
		response.put("socialMediaLink", alumniVerification.getSocialMediaLink());
		response.put("comment", alumniVerification.getComment());
		response.put("status", alumniVerification.getStatus());
		response.put("createAt", alumniVerification.getCreateAt());
		response.put("isDelete", alumniVerification.getIsDelete());
		response.put("fullName", userRepository.findFullNameByUserId(user_id));
		response.put("avatarUrl", userRepository.findAvatarUrlByUserId(user_id));

		return ResponseEntity.ok(response);
	}

	@PreAuthorize("hasAnyAuthority('AlumniVerify.Create')")
	@PostMapping("/alumni-verification")
	public ResponseEntity<String> createAlumniVerification(@RequestHeader("userId") String userId,
			@RequestParam(value = "avatar", required = false) MultipartFile avatar,
			@RequestParam("fullName") String fullName,
			@RequestParam(value = "studentId", required = false) String studentId,
			@RequestParam(value = "beginningYear", required = false, defaultValue = "0") Integer beginningYear,
			@RequestParam(value = "socialMediaLink", required = false) String socialMediaLink,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId) {

		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (!optionalUser.isPresent()) {
			throw new AppException(20400, "Người dùng không tồn tại", HttpStatus.NOT_FOUND);
		}
		UserModel user = optionalUser.get();
		user.setSocialMediaLink(socialMediaLink);
		FacultyModel faculty = new FacultyModel();
		if (beginningYear.equals(0)) {
			beginningYear = null;
		}
		if (facultyId.equals(0)) {
			faculty = null;
		} else {
			faculty.setId(facultyId);
			user.setFaculty(new FacultyModel(facultyId));
		}
		VerifyAlumniModel verifyAlumni = new VerifyAlumniModel(user, studentId, beginningYear, socialMediaLink,
				faculty);

		try {
			if (avatar != null) {
				String avatarUrl = imageUtils.saveImageToStorage(imageUtils.getAvatarPath(), avatar, userId);
				System.out.println(avatarUrl);
				user.setAvatarUrl(avatarUrl);
			}

			userRepository.setDataFirstVerifyAlumni(userId, fullName, socialMediaLink, faculty);
			if (studentId != null || beginningYear != null || socialMediaLink != null || facultyId != 0) {
				verifyAlumniRepository.save(verifyAlumni);
			} else {
				userRepository.save(user);
			}
		} catch (DataIntegrityViolationException e) {
			throw new AppException(20401, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(20402, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PreAuthorize("hasAnyAuthority('AlumniVerify.Edit')")
	@PutMapping("/alumni-verification/{id}/verify")
	public ResponseEntity<String> updateAlumniVerificationStatus(@PathVariable String id,
			@RequestBody Map<String, Object> requestBody) {
		VerifyAlumniModel va = verifyAlumniRepository.findByIdAndIsDeleteEqualsAndStatusEquals(id, false,
				VerifyAlumniModel.Status.PENDING);
		if (va == null) {
			throw new AppException(20500, "Không tìm thấy thông tin xác thực cựu sinh viên", HttpStatus.NOT_FOUND);
		}

		String reqStatus = (String) requestBody.get("status");
		String comment = (String) requestBody.get("comment");
		try {
			VerifyAlumniModel.Status status = VerifyAlumniModel.Status.valueOf(reqStatus);
			if (status == VerifyAlumniModel.Status.PENDING) {
				throw new AppException(20501, "Trạng thái không hợp lệ", HttpStatus.BAD_REQUEST);
			}

			va.setStatus(status);
			va.setComment(comment);
			verifyAlumniRepository.save(va);

			if (status.equals(VerifyAlumniModel.Status.APPROVED)) {
				// Change role
				userRepository.updateRoleFromGuestToAlumni(va.getUser().getId());
			}
		} catch (IllegalArgumentException e) {
			throw new AppException(20501, "Trạng thái không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.ok("");
	}

	@PreAuthorize("hasAnyAuthority('User.Create')")
	@PostMapping("")
	public ResponseEntity<String> adminCreateUser(@RequestBody UserModel req) {
		if (req.getEmail() == null || req.getEmail().equals("")) {
			throw new AppException(20600, "Email không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (req.getFullName() == null || req.getFullName().equals("")) {
			throw new AppException(20601, "Họ tên không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (req.getRoles() == null || req.getRoles().size() == 0) {
			throw new AppException(20602, "Vai trò không được để trống", HttpStatus.BAD_REQUEST);
		}

		UserModel newUser = new UserModel();
		newUser.setId(UUID.randomUUID().toString());
		newUser.setEmail(req.getEmail());
		String pwd = UserConfig.generateRandomPassword(10);
		newUser.setPass(passwordEncoder.encode(pwd));
		newUser.setFullName(req.getFullName());
		PasswordHistoryModel passwordHistory = new PasswordHistoryModel(newUser.getId(), newUser.getPass(), true,
				new Date());

		Set<RoleModel> roles = new HashSet<>();
		for (RoleModel role : req.getRoles()) {
			Optional<RoleModel> roleModelOptional = roleRepository.findById(role.getId());
			if (!roleModelOptional.isPresent()) {
				throw new AppException(20603, "Vai trò không tồn tại", HttpStatus.NOT_FOUND);

			}
			roles.add(roleModelOptional.get());
		}

		newUser.setRoles(roles);

		try {
			userRepository.save(newUser);
			passwordHistoryRepository.save(passwordHistory);
			emailSenderUtils.sendPasswordEmail(req.getEmail(), pwd);
		} catch (DataIntegrityViolationException e) {
			System.err.println(e);
			throw new AppException(20604, "Email đã tồn tại", HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			System.err.println(e);
			throw new AppException(20605, "Lỗi khi gửi email", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PreAuthorize("hasAnyAuthority('User.Edit')")
	@PutMapping("/{id}/status")
	public ResponseEntity<String> adminUpdateUserStatus(@PathVariable String id,
			@RequestParam(value = "statusId", required = false) Integer statusId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (!optionalUser.isPresent()) {
			throw new AppException(20700, "Người dùng không tồn tại", HttpStatus.NOT_FOUND);
		}

		UserModel user = optionalUser.get();

		if (statusId == null) {
			throw new AppException(20701, "Trạng thái tài khoản không được để trống", HttpStatus.BAD_REQUEST);
		}

		boolean statusExists = statusUserGroupRepository.existsById(statusId);
		if (!statusExists) {
			throw new AppException(20702, "Trạng thái tài khoản không tồn tại", HttpStatus.BAD_REQUEST);
		}

		user.setStatusId(statusId);
		userRepository.save(user);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('User.Edit')")
	@PutMapping("/{id}/role")
	public ResponseEntity<String> adminUpdateUserRole(@PathVariable String id,
			@RequestParam(value = "roleIds", required = false) List<Integer> roleIds) {

		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (!optionalUser.isPresent()) {
			throw new AppException(20800, "Người dùng không tồn tại", HttpStatus.NOT_FOUND);
		}

		UserModel user = optionalUser.get();
		if (roleIds == null || roleIds.isEmpty()) {
			throw new AppException(20801, "Vai trò không được để trống", HttpStatus.BAD_REQUEST);
		}

		Set<RoleModel> roles = new HashSet<>();
		for (Integer roleId : roleIds) {
			RoleModel role = roleRepository.findById(roleId)
					.orElseThrow(() -> new AppException(20802, "Vai trò không tồn tại", HttpStatus.BAD_REQUEST));
			roles.add(role);
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("/count/role")
	public ResponseEntity<Long> getSearchResultCount(@RequestParam(value = "roleId") List<Integer> roleIds) {
		try {
			if (roleIds == null || roleIds.isEmpty()) {
				return ResponseEntity.status(HttpStatus.OK).body(userRepository.countAllUsers());
			}
			return ResponseEntity.status(HttpStatus.OK).body(userRepository.countUsersByRoleId(roleIds));
		} catch (Exception e) {
			throw new AppException(20903, "Lỗi khi lấy số lượng người dùng. Vui lòng thử lại", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getSearchResult(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "orderBy", required = false, defaultValue = "fullName") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "asc") String order,
			@RequestParam(value = "fullName", required = false) String fullName,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "roleIds", required = false) List<Integer> roleIds) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<UserSearchDto> users = null;

			users = userRepository.searchUsers(fullName, email, roleIds, pageable);

			result.put("totalPages", users.getTotalPages());
			result.put("users", users.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(21001, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new AppException(21002, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<HashMap<String, Object>> getUser(@PathVariable String id) {
		Optional<UserSearchDto> user = userRepository.findByIdCustom(id);
		if (user.isEmpty()) {
			throw new AppException(21101, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
		}

		HashMap<String, Object> result = new HashMap<String, Object>();

		result.put("user", user.get());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/profile/{id}")
	public ResponseEntity<Map<String, Object>> getProfileInfo(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);

		if (optionalUser.isEmpty()) {
			throw new AppException(21201, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		UserModel user = optionalUser.get();
		Optional<AlumniModel> optionalAlumni = alumniRepository.findByUserId(id);

		Map<String, Object> response = new HashMap<>();
		response.put("userId", user.getId());
		response.put("avatarUrl", user.getAvatarUrl());
		response.put("coverUrl", user.getCoverUrl());

		response.put("fullName", user.getFullName());
		response.put("faculty", user.getFaculty());
		response.put("sex", user.getSex());
		response.put("dob", user.getDob());
		response.put("socialMediaLink", user.getSocialMediaLink());

		if (!optionalAlumni.isEmpty()) {
			AlumniModel alumni = optionalAlumni.get();
			response.put("alumClass", alumni.getAlumClass());
			response.put("graduationYear", alumni.getGraduationYear());
		} else {
			response.put("alumClass", null);
			response.put("graduationYear", null);
		}

		response.put("email", user.getEmail());
		response.put("phone", user.getPhone());
		response.put("aboutMe", user.getAboutMe());

		Optional<VerifyAlumniModel> alumniVerificationOptional = verifyAlumniRepository
				.findByUserIdAndIsDeleteEquals(id, false);

		if (!alumniVerificationOptional.isEmpty()) {
			VerifyAlumniModel alumniVerification = alumniVerificationOptional.get();

			response.put("verifyAlumniStatus", alumniVerification.getStatus());
			response.put("studentId", alumniVerification.getStudentId());
			response.put("beginningYear", alumniVerification.getBeginningYear());
		} else {
			response.put("verifyAlumniStatus", null);
			response.put("studentId", null);
			response.put("beginningYear", null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/profile/{id}")
	public ResponseEntity<String> updateBasicProfileInfo(@PathVariable String id,
			@RequestParam(value = "fullName", required = false) String fullName,
			@RequestParam(value = "facultyId", required = false) Integer facultyId,
			@RequestParam(value = "sexId", required = false) Integer sexId,
			@RequestParam(value = "dob", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dob,
			@RequestParam(value = "socialMediaLink", required = false) String socialMediaLink,
			@RequestParam(value = "alumClass", required = false) String alumClass,
			@RequestParam(value = "graduationYear", required = false) Integer graduationYear,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "aboutMe", required = false) String aboutMe) {
		Optional<UserModel> optionalUser = userRepository.findById(id);

		if (optionalUser.isEmpty()) {
			throw new AppException(21301, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		UserModel user = optionalUser.get();
		Optional<AlumniModel> optionalAlumni = alumniRepository.findByUserId(id);
		boolean isPut = false;

		if (StringUtils.isNotEmpty(fullName)) {
			user.setFullName(fullName);
			isPut = true;
		}
		if (facultyId != null) {
			user.setFaculty(new FacultyModel(facultyId));
			isPut = true;
		}
		if (sexId != null) {
			user.setSex(new SexModel(sexId));
			isPut = true;
		}
		if (dob != null) {
			user.setDob(dob);
			isPut = true;
		}
		if (StringUtils.isNotEmpty(socialMediaLink)) {
			user.setSocialMediaLink(socialMediaLink);
			isPut = true;
		}
		AlumniModel alumni = null;
		if (!optionalAlumni.isEmpty()) {
			alumni = optionalAlumni.get();
			if (StringUtils.isNotEmpty(alumClass)) {
				alumni.setAlumClass(alumClass);
				isPut = true;
			}
			if (graduationYear != null) {
				alumni.setGraduationYear(graduationYear);
				isPut = true;
			}
		}

		if (StringUtils.isNotEmpty(phone)) {
			user.setPhone(phone);
			isPut = true;
		}
		if (StringUtils.isNotEmpty(aboutMe)) {
			user.setAboutMe(aboutMe);
			isPut = true;
		}

		if (isPut) {
			userRepository.save(user);
			if (!optionalAlumni.isEmpty()) {
				alumniRepository.save(alumni);
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PutMapping("/profile/{id}/pending-info")
	public ResponseEntity<String> updateUserPendingInfo(@PathVariable String id,
			@RequestParam(value = "studentId", required = false) String studentId,
			@RequestParam(value = "beginningYear", required = false) Integer beginningYear) {
		Optional<UserModel> optionalUser = userRepository.findById(id);

		if (optionalUser.isEmpty()) {
			throw new AppException(21401, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		boolean isPut = false;

		Optional<VerifyAlumniModel> alumniVerificationOptional = verifyAlumniRepository
				.findByUserIdAndIsDeleteEquals(id, false);
		VerifyAlumniModel alumniVerification = null;
		if (!alumniVerificationOptional.isEmpty()) {
			alumniVerification = alumniVerificationOptional.get();
			if (StringUtils.isNotEmpty(studentId)) {
				alumniVerification.setStudentId(studentId);
				isPut = true;
			}
			if (beginningYear != null) {
				alumniVerification.setBeginningYear(beginningYear);
				isPut = true;
			}
		}

		if (isPut) {
			verifyAlumniRepository.save(alumniVerification);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PutMapping("/profile/{id}/avatar")
	public ResponseEntity<String> updateProfileAvatar(@PathVariable String id,
			@RequestParam(value = "avatarUrl", required = false) MultipartFile avatarUrl) {
		Optional<UserModel> optionalUser = userRepository.findById(id);

		if (optionalUser.isEmpty()) {
			throw new AppException(21501, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}
		UserModel user = optionalUser.get();
		if (avatarUrl != null && !avatarUrl.isEmpty()) {
			try {
				String avatarPath = imageUtils.saveImageToStorage(imageUtils.getAvatarPath(id), avatarUrl, "avatarUrl");
				user.setAvatarUrl(avatarPath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(21502, "Lỗi khi tải ảnh lên", HttpStatus.BAD_REQUEST);
			}
		}
		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PutMapping("/profile/{id}/cover")
	public ResponseEntity<String> updateProfileCover(@PathVariable String id,
			@RequestParam(value = "coverUrl", required = false) MultipartFile coverUrl) {
		Optional<UserModel> optionalUser = userRepository.findById(id);

		if (optionalUser.isEmpty()) {
			throw new AppException(21601, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}
		UserModel user = optionalUser.get();
		if (coverUrl != null && !coverUrl.isEmpty()) {
			try {
				String coverPath = imageUtils.saveImageToStorage(imageUtils.getCoverPath(id), coverUrl, "coverUrl");
				user.setAvatarUrl(coverPath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(21602, "Lỗi khi tải ảnh lên", HttpStatus.BAD_REQUEST);
			}
		}
		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("profile/{id}/job")
	public ResponseEntity<Map<String, Object>> getAllJobs(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(21701, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		List<JobModel> jobs = jobRepository.findByUserId(id);
		if (jobs.isEmpty()) {
			throw new AppException(21702, "Không có công việc nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}

		HashMap<String, Object> result = new HashMap<>();
		result.put("jobs", jobs);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("profile/{id}/job/{jobId}")
	public ResponseEntity<IJobDto> getJobById(@PathVariable String id, @PathVariable String jobId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(21801, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<IJobDto> optionalJob = jobRepository.findByJobId(jobId);
		if (optionalJob.isEmpty()) {
			throw new AppException(21802, "Không có công việc nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalJob.get());
	}

	@PostMapping("profile/{id}/create-job")
	public ResponseEntity<String> createJob(@PathVariable String id, @RequestBody JobDto newJobDto) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(21901, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		if (newJobDto.getCompanyName() == null || newJobDto.getCompanyName().isBlank()) {
			throw new AppException(21902, "Tên công ty không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (newJobDto.getPosition() == null || newJobDto.getPosition().isBlank()) {
			throw new AppException(21903, "Chức vụ không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<JobModel> optionalJob = jobRepository.findByUserIdAndCompanyNameAndPosition(id,
				newJobDto.getCompanyName(), newJobDto.getPosition());
		JobModel newJob;
		if (optionalJob.isPresent()) {
			if (!optionalJob.get().getIsDelete()) {
				throw new AppException(21904, "Công việc đã tồn tại", HttpStatus.BAD_REQUEST);
			} else {
				newJob = optionalJob.get();
				if (newJobDto.getStartTime() == null) {
					newJob.setStartTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
				} else {
					newJob.setStartTime(newJobDto.getStartTime());
				}
				newJob.setEndTime(newJobDto.getEndTime());
				newJob.setPrivacy(Privacy.valueOf(newJobDto.getPrivacy().toUpperCase()));
				newJob.setIsWorking(newJobDto.getIsWorking());
				newJob.setIsDelete(false);
			}
		} else {
			newJob = new JobModel();
			newJob.setJobId(UUID.randomUUID().toString());
			newJob.setUserId(id);
			newJob.setCompanyName(newJobDto.getCompanyName());
			newJob.setPosition(newJobDto.getPosition());
			if (newJobDto.getStartTime() == null) {
				newJob.setStartTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} else {
				newJob.setStartTime(newJobDto.getStartTime());
			}
			newJob.setEndTime(newJobDto.getEndTime());
			newJob.setPrivacy(Privacy.valueOf(newJobDto.getPrivacy().toUpperCase()));
			newJob.setIsWorking(newJobDto.getIsWorking());
		}

		jobRepository.save(newJob);

		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("profile/{id}/update-job/{jobId}")
	public ResponseEntity<String> updateJob(@PathVariable String id, @PathVariable String jobId,
			@RequestBody JobDto updatedJobDto) {

		Optional<JobModel> optionalJob = jobRepository.findByUserIdAndJobId(id, jobId);
		if (optionalJob.isEmpty()) {
			throw new AppException(22001, "Không tìm thấy công việc", HttpStatus.NOT_FOUND);
		}

		JobModel jobToUpdate = optionalJob.get();

		Optional<JobModel> existingJob = jobRepository.findByUserIdAndCompanyNameAndPosition(id,
				updatedJobDto.getCompanyName(), updatedJobDto.getPosition());
		if (existingJob.isPresent() && !existingJob.get().getJobId().equals(jobId)) {
			throw new AppException(22002, "Không thể cập nhật vì đã tồn tại công việc với cùng công ty và chức vụ",
					HttpStatus.BAD_REQUEST);
		}

		if (updatedJobDto.getCompanyName() != null || !updatedJobDto.getCompanyName().isEmpty()) {
			jobToUpdate.setCompanyName(updatedJobDto.getCompanyName());
		}
		if (updatedJobDto.getPosition() != null && !updatedJobDto.getPosition().isEmpty()) {
			jobToUpdate.setPosition(updatedJobDto.getPosition());
		}
		if (updatedJobDto.getStartTime() != null) {
			jobToUpdate.setStartTime(updatedJobDto.getStartTime());
		}
		if (updatedJobDto.getEndTime() != null) {
			jobToUpdate.setEndTime(updatedJobDto.getEndTime());
		}
		if (updatedJobDto.getPrivacy() != null) {
			jobToUpdate.setPrivacy(Privacy.valueOf(updatedJobDto.getPrivacy().toUpperCase()));
		}
		if (updatedJobDto.getIsWorking() != null) {
			jobToUpdate.setIsWorking(updatedJobDto.getIsWorking());
		}
		if (updatedJobDto.getIsDelete() != null) {
			jobToUpdate.setIsDelete(updatedJobDto.getIsDelete());
		}

		jobRepository.save(jobToUpdate);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("profile/{id}/education")
	public ResponseEntity<Map<String, Object>> getAllEducations(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22101, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		List<EducationModel> edus = educationRepository.findByUserId(id);
		if (edus.isEmpty()) {
			throw new AppException(22102, "Không có bằng cấp nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}

		HashMap<String, Object> result = new HashMap<>();
		result.put("edus", edus);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("profile/{id}/education/{educationId}")
	public ResponseEntity<IEducationDto> getEducationById(@PathVariable String id, @PathVariable String educationId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22201, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<IEducationDto> optionalEdu = educationRepository.findByEducationId(educationId);
		if (optionalEdu.isEmpty()) {
			throw new AppException(22202, "Không có bằng cấp nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalEdu.get());
	}

	@PostMapping("profile/{id}/create-education")
	public ResponseEntity<String> createEducation(@PathVariable String id, @RequestBody EducationDto newEducationDto) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22301, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		if (newEducationDto.getSchoolName() == null || newEducationDto.getSchoolName().isBlank()) {
			throw new AppException(22302, "Tên trường học không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (newEducationDto.getDegree() == null || newEducationDto.getDegree().isBlank()) {
			throw new AppException(22303, "Tên bằng cấp không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<EducationModel> optionalEdu = educationRepository.findByUserIdAndSchoolNameAndDegree(id,
				newEducationDto.getSchoolName(), newEducationDto.getDegree());
		EducationModel newEdu;
		if (optionalEdu.isPresent()) {
			if (!optionalEdu.get().getIsDelete()) {
				throw new AppException(22304, "Học vấn đã tồn tại", HttpStatus.BAD_REQUEST);
			} else {
				newEdu = optionalEdu.get();
				if (newEducationDto.getStartTime() == null) {
					newEdu.setStartTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
				} else {
					newEdu.setStartTime(newEducationDto.getStartTime());
				}
				newEdu.setEndTime(newEducationDto.getEndTime());
				newEdu.setPrivacy(Privacy.valueOf(newEducationDto.getPrivacy().toUpperCase()));
				newEdu.setIsLearning(newEducationDto.getIsLearning());
				newEdu.setIsDelete(false);
			}
		} else {
			newEdu = new EducationModel();
			newEdu.setEducationId(UUID.randomUUID().toString());
			newEdu.setUserId(id);
			newEdu.setSchoolName(newEducationDto.getSchoolName());
			newEdu.setDegree(newEducationDto.getDegree());
			if (newEducationDto.getStartTime() == null) {
				newEdu.setStartTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} else {
				newEdu.setStartTime(newEducationDto.getStartTime());
			}
			newEdu.setEndTime(newEducationDto.getEndTime());
			newEdu.setPrivacy(Privacy.valueOf(newEducationDto.getPrivacy().toUpperCase()));
			newEdu.setIsLearning(newEducationDto.getIsLearning());
		}

		educationRepository.save(newEdu);

		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("profile/{id}/update-education/{educationId}")
	public ResponseEntity<String> updateEducation(@PathVariable String id, @PathVariable String educationId,
			@RequestBody EducationDto updatedEducationDto) {

		Optional<EducationModel> optionalEdu = educationRepository.findByUserIdAndEducationId(id, educationId);
		if (optionalEdu.isEmpty()) {
			throw new AppException(22401, "Không tìm thấy học vấn", HttpStatus.NOT_FOUND);
		}

		EducationModel eduToUpdate = optionalEdu.get();

		Optional<EducationModel> existingEdu = educationRepository.findByUserIdAndSchoolNameAndDegree(id,
				updatedEducationDto.getSchoolName(), updatedEducationDto.getDegree());
		if (existingEdu.isPresent() && !existingEdu.get().getEducationId().equals(educationId)) {
			throw new AppException(22402, "Không thể cập nhật vì đã tồn tại học vấn với cùng trường học và bằng cấp",
					HttpStatus.BAD_REQUEST);
		}

		if (updatedEducationDto.getSchoolName() != null || !updatedEducationDto.getSchoolName().isEmpty()) {
			eduToUpdate.setSchoolName(updatedEducationDto.getSchoolName());
		}
		if (updatedEducationDto.getDegree() != null && !updatedEducationDto.getDegree().isEmpty()) {
			eduToUpdate.setDegree(updatedEducationDto.getDegree());
		}
		if (updatedEducationDto.getStartTime() != null) {
			eduToUpdate.setStartTime(updatedEducationDto.getStartTime());
		}
		if (updatedEducationDto.getEndTime() != null) {
			eduToUpdate.setEndTime(updatedEducationDto.getEndTime());
		}
		if (updatedEducationDto.getPrivacy() != null) {
			eduToUpdate.setPrivacy(Privacy.valueOf(updatedEducationDto.getPrivacy().toUpperCase()));
		}
		if (updatedEducationDto.getIsLearning() != null) {
			eduToUpdate.setIsLearning(updatedEducationDto.getIsLearning());
		}
		if (updatedEducationDto.getIsDelete() != null) {
			eduToUpdate.setIsDelete(updatedEducationDto.getIsDelete());
		}

		educationRepository.save(eduToUpdate);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("profile/{id}/achievement")
	public ResponseEntity<Map<String, Object>> getAllAchievements(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22501, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		List<AchievementModel> achievements = achievementRepository.findByUserId(id);
		if (achievements.isEmpty()) {
			throw new AppException(22502, "Không có thành tựu nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}

		HashMap<String, Object> result = new HashMap<>();
		result.put("achievements", achievements);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("profile/{id}/achievement/{achievementId}")
	public ResponseEntity<IAchievementDto> getAchievementById(@PathVariable String id,
			@PathVariable String achievementId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22601, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<IAchievementDto> optionalAchievement = achievementRepository.findByAchievementId(achievementId);
		if (optionalAchievement.isEmpty()) {
			throw new AppException(22602, "Không có thành tựu nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalAchievement.get());
	}

	@PostMapping("profile/{id}/create-achievement")
	public ResponseEntity<String> createAchievement(@PathVariable String id,
			@RequestBody AchievementDto newAchievementDto) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22701, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		if (newAchievementDto.getName() == null || newAchievementDto.getName().isBlank()) {
			throw new AppException(22702, "Tên thành tựu không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (newAchievementDto.getType() == null || newAchievementDto.getType().isBlank()) {
			throw new AppException(22703, "Loại bằng cấp không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<AchievementModel> optionalAchievement = achievementRepository.findByUserIdAndNameAndType(id,
				newAchievementDto.getName(), newAchievementDto.getType());
		AchievementModel newAchievement;
		if (optionalAchievement.isPresent()) {
			if (!optionalAchievement.get().getIsDelete()) {
				throw new AppException(22704, "Thành tựu đã tồn tại", HttpStatus.BAD_REQUEST);
			} else {
				newAchievement = optionalAchievement.get();
				if (newAchievementDto.getTime() == null) {
					newAchievement.setTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
				} else {
					newAchievement.setTime(newAchievementDto.getTime());
				}
				newAchievement.setPrivacy(Privacy.valueOf(newAchievementDto.getPrivacy().toUpperCase()));
				newAchievement.setIsDelete(false);
			}
		} else {
			newAchievement = new AchievementModel();
			newAchievement.setAchievementId(UUID.randomUUID().toString());
			newAchievement.setUserId(id);
			newAchievement.setName(newAchievementDto.getName());
			newAchievement.setType(newAchievementDto.getType());
			if (newAchievementDto.getTime() == null) {
				newAchievement.setTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} else {
				newAchievement.setTime(newAchievementDto.getTime());
			}
			newAchievement.setPrivacy(Privacy.valueOf(newAchievementDto.getPrivacy().toUpperCase()));
		}

		achievementRepository.save(newAchievement);

		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("profile/{id}/update-achievement/{achievementId}")
	public ResponseEntity<String> updateEducation(@PathVariable String id, @PathVariable String achievementId,
			@RequestBody AchievementDto updatedAchievementDto) {

		Optional<AchievementModel> optionalAchievement = achievementRepository.findByUserIdAndAchievementId(id,
				achievementId);
		if (optionalAchievement.isEmpty()) {
			throw new AppException(22801, "Không tìm thấy thành tựu", HttpStatus.NOT_FOUND);
		}

		AchievementModel achievementToUpdate = optionalAchievement.get();

		Optional<AchievementModel> existingAchievement = achievementRepository.findByUserIdAndNameAndType(id,
				updatedAchievementDto.getName(), updatedAchievementDto.getType());
		if (existingAchievement.isPresent() && !existingAchievement.get().getAchievementId().equals(achievementId)) {
			throw new AppException(22802,
					"Không thể cập nhật vì đã tồn tại thành tựu với cùng tên thành tựu và loại thành tựu",
					HttpStatus.BAD_REQUEST);
		}

		if (updatedAchievementDto.getName() != null || !updatedAchievementDto.getName().isEmpty()) {
			achievementToUpdate.setName(updatedAchievementDto.getName());
		}
		if (updatedAchievementDto.getType() != null && !updatedAchievementDto.getType().isEmpty()) {
			achievementToUpdate.setType(updatedAchievementDto.getType());
		}
		if (updatedAchievementDto.getTime() != null) {
			achievementToUpdate.setTime(updatedAchievementDto.getTime());
		}
		if (updatedAchievementDto.getPrivacy() != null) {
			achievementToUpdate.setPrivacy(Privacy.valueOf(updatedAchievementDto.getPrivacy().toUpperCase()));
		}
		if (updatedAchievementDto.getIsDelete() != null) {
			achievementToUpdate.setIsDelete(updatedAchievementDto.getIsDelete());
		}

		achievementRepository.save(achievementToUpdate);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

}