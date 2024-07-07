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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import hcmus.alumni.userservice.dto.AlumniDto;
import hcmus.alumni.userservice.dto.EducationDto;
import hcmus.alumni.userservice.dto.FriendIdRequestDto;
import hcmus.alumni.userservice.dto.FriendRequestActionDTO;
import hcmus.alumni.userservice.dto.IAchievementDto;
import hcmus.alumni.userservice.dto.IAlumniProfileDto;
import hcmus.alumni.userservice.dto.IEducationDto;
import hcmus.alumni.userservice.dto.IFriendDto;
import hcmus.alumni.userservice.dto.IFriendRequestDto;
import hcmus.alumni.userservice.dto.IJobDto;
import hcmus.alumni.userservice.dto.ISuggestionUserDto;
import hcmus.alumni.userservice.dto.IUserProfileDto;
import hcmus.alumni.userservice.dto.IUserSearchDto;
import hcmus.alumni.userservice.dto.IVerifyAlumniProfileDto;
import hcmus.alumni.userservice.dto.JobDto;
import hcmus.alumni.userservice.dto.ProfileRequestDto;
import hcmus.alumni.userservice.dto.UserDto;
import hcmus.alumni.userservice.dto.VerifyAlumniDto;
import hcmus.alumni.userservice.dto.VerifyAlumniRequestDto;
import hcmus.alumni.userservice.exception.AppException;
import hcmus.alumni.userservice.model.AchievementModel;
import hcmus.alumni.userservice.model.AlumniModel;
import hcmus.alumni.userservice.model.EducationModel;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.FriendId;
import hcmus.alumni.userservice.model.FriendModel;
import hcmus.alumni.userservice.model.FriendRequestAction;
import hcmus.alumni.userservice.model.FriendRequestId;
import hcmus.alumni.userservice.model.FriendRequestModel;
import hcmus.alumni.userservice.model.JobModel;
import hcmus.alumni.userservice.model.PasswordHistoryModel;
import hcmus.alumni.userservice.model.RoleModel;
import hcmus.alumni.userservice.model.SexModel;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.repository.AchievementRepository;
import hcmus.alumni.userservice.repository.AlumniRepository;
import hcmus.alumni.userservice.repository.EducationRepository;
import hcmus.alumni.userservice.repository.FriendRepository;
import hcmus.alumni.userservice.repository.FriendRequestRepository;
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

	@Autowired
	private FriendRepository friendRepository;

	@Autowired
	private FriendRequestRepository friendRequestRepository;

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

				AlumniModel alumni = new AlumniModel();
				alumni.setUserId(va.getUser().getId());
				alumni.setStudentId(va.getStudentId());
				alumni.setBeginningYear(va.getBeginningYear());
				alumniRepository.save(alumni);
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
			throw new AppException(20900, "Lỗi khi lấy số lượng người dùng. Vui lòng thử lại", HttpStatus.BAD_REQUEST);
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
			Page<IUserSearchDto> users = null;

			users = userRepository.searchUsers(fullName, email, roleIds, pageable);

			result.put("totalPages", users.getTotalPages());
			result.put("users", users.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(21000, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new AppException(21001, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<HashMap<String, Object>> getUser(@PathVariable String id) {
		Optional<IUserSearchDto> user = userRepository.findByIdCustom(id);
		if (user.isEmpty()) {
			throw new AppException(21100, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
		}

		HashMap<String, Object> result = new HashMap<String, Object>();

		result.put("user", user.get());
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}/profile")
	public ResponseEntity<Map<String, Object>> getProfileInfo(@RequestHeader("userId") String userId,
			@PathVariable String id) {

		Optional<IUserProfileDto> optionalUser = userRepository.findUserProfileById(id);

		if (optionalUser.isEmpty()) {
			throw new AppException(21200, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		IUserProfileDto user = optionalUser.get();
		Optional<IAlumniProfileDto> optionalAlumni = alumniRepository.findAlumniProfileById(id);

		Map<String, Object> response = new HashMap<>();

		Optional<IVerifyAlumniProfileDto> alumniVerificationOptional = verifyAlumniRepository
				.findByUserIdAndIsDelete(id, false);

		response.put("user", user);
		response.put("alumni", optionalAlumni);
		response.put("alumniVerification", alumniVerificationOptional);
		
		//add status for current user and Profile user
		if (userId != null && id != null && !userId.equals(id)) {
			String status = "Not Friend";
			Optional<FriendRequestModel> optionalFriendRequest = friendRequestRepository
					.findByUserIdAndFriendIdAndIsDelete(userId, id);
			if (optionalFriendRequest != null && !optionalFriendRequest.isEmpty()
					&& optionalFriendRequest.isPresent()) {
				status = "Pending";
			} else {
				Optional<FriendModel> optionalFriend = friendRepository.findByUserIdAndFriendIdAndIsDelete(userId, id);
				if (optionalFriend != null && !optionalFriend.isEmpty()
						&& optionalFriend.isPresent()) {
					status = "true";
				}

			}
			response.put("isFriendStatus", status);
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PutMapping("/profile")
	public ResponseEntity<String> updateBasicProfileInfo(@RequestHeader("userId") String userId,
			@RequestBody ProfileRequestDto profileRequestDto) {
		Optional<UserModel> optionalUser = userRepository.findById(userId);

		if (optionalUser.isEmpty()) {
			throw new AppException(21300, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		UserModel user = optionalUser.get();
		UserDto userDto = profileRequestDto.getUser();
		AlumniDto alumniDto = profileRequestDto.getAlumni();
		if (userDto != null) {
			if (userDto.getFullName() != null) {
				user.setFullName(userDto.getFullName());
			}
			if (userDto.getFacultyId() != null) {
				user.setFaculty(new FacultyModel(userDto.getFacultyId()));
			}
			if (userDto.getSexId() != null) {
				user.setSex(new SexModel(userDto.getSexId()));
			}
			if (userDto.getSocialMediaLink() != null) {
				user.setSocialMediaLink(userDto.getSocialMediaLink());
			}
			if (userDto.getPhone() != null) {
				user.setPhone(userDto.getPhone());
			}
			if (userDto.getAboutMe() != null) {
				user.setAboutMe(userDto.getAboutMe());
			}
			if (userDto.getDob() != null) {
				user.setDob(userDto.getDob());
			}
		}

		Optional<AlumniModel> optionalAlumni = alumniRepository.findByUserId(userId);
		if (optionalAlumni.isPresent() && alumniDto != null) {
			AlumniModel alumni = optionalAlumni.get();
			if (alumniDto.getAlumClass() != null) {
				alumni.setAlumClass(alumniDto.getAlumClass());
			}
			if (alumniDto.getGraduationYear() != null) {
				alumni.setGraduationYear(alumniDto.getGraduationYear());
			}
			alumniRepository.save(alumni);
		}
		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('User.Edit')")
	@PutMapping("/alumni-verification")
	public ResponseEntity<String> updateUserPendingInfo(@RequestHeader("userId") String userId,
			@RequestBody VerifyAlumniRequestDto requestDto) {

		Optional<VerifyAlumniModel> alumniVerificationOptional = verifyAlumniRepository
				.findTopByUserIdAndIsDeleteEqualsOrderByCreateAtDesc(userId, false);

		VerifyAlumniModel alumniVerification = alumniVerificationOptional.orElseThrow(
				() -> new AppException(21400, "Không tìm thấy thông tin xác nhận cựu sinh viên", HttpStatus.NOT_FOUND));

		if (alumniVerification.getStatus() == VerifyAlumniModel.Status.PENDING) {
			throw new AppException(21401, "Không thể cập nhật thông tin khi đang trong quá trình xác thực",
					HttpStatus.BAD_REQUEST);
		}
		if (alumniVerification.getStatus() == VerifyAlumniModel.Status.APPROVED) {
			throw new AppException(21402, "Không thể cập nhật thông tin vì người dùng đã được xác thực",
					HttpStatus.BAD_REQUEST);
		}

		boolean isUpdated = false;

		if (StringUtils.isNotEmpty(requestDto.getStudentId())) {
			alumniVerification.setStudentId(requestDto.getStudentId());
			isUpdated = true;
		}
		if (requestDto.getBeginningYear() != null) {
			alumniVerification.setBeginningYear(requestDto.getBeginningYear());
			isUpdated = true;
		}

		if (isUpdated) {
			verifyAlumniRepository.save(alumniVerification);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PutMapping("/profile/avatar")
	public ResponseEntity<String> updateProfileAvatar(@RequestHeader("userId") String userId,
			@RequestParam(value = "avatar", required = true) MultipartFile avatar) {
		Optional<UserModel> optionalUser = userRepository.findById(userId);

		if (optionalUser.isEmpty()) {
			throw new AppException(21500, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}
		UserModel user = optionalUser.get();
		if (avatar != null && !avatar.isEmpty()) {
			try {
				String avatarPath = imageUtils.saveImageToStorage(imageUtils.getAvatarPath(), avatar, userId);
				user.setAvatarUrl(avatarPath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(21501, "Lỗi khi tải ảnh lên", HttpStatus.BAD_REQUEST);
			}
		}
		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body(user.getAvatarUrl());
	}

	@PutMapping("/profile/cover")
	public ResponseEntity<String> updateProfileCover(@RequestHeader("userId") String userId,
			@RequestParam(value = "cover", required = true) MultipartFile cover) {
		Optional<UserModel> optionalUser = userRepository.findById(userId);

		if (optionalUser.isEmpty()) {
			throw new AppException(21600, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}
		UserModel user = optionalUser.get();
		if (cover != null && !cover.isEmpty()) {
			try {
				String coverPath = imageUtils.saveImageToStorage(imageUtils.getCoverPath(), cover, userId);
				user.setCoverUrl(coverPath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(21601, "Lỗi khi tải ảnh lên", HttpStatus.BAD_REQUEST);
			}
		}
		userRepository.save(user);
		return ResponseEntity.status(HttpStatus.OK).body(user.getCoverUrl());
	}

	@GetMapping("/{id}/profile/job")
	public ResponseEntity<Map<String, Object>> getAllJobs(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(21700, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		List<JobModel> jobs = jobRepository.findByUserId(id);

		HashMap<String, Object> result = new HashMap<>();
		result.put("jobs", jobs);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("{id}/profile/job/{jobId}")
	public ResponseEntity<IJobDto> getJobById(@PathVariable String id, @PathVariable String jobId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(21800, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<IJobDto> optionalJob = jobRepository.findByJobId(jobId);
		if (optionalJob.isEmpty()) {
			throw new AppException(21801, "Không có công việc nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalJob.get());
	}

	@PostMapping("profile/job")
	public ResponseEntity<String> createJob(@RequestHeader String userId, @RequestBody JobDto newJobDto) {
		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new AppException(21900, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		if (newJobDto.getCompanyName() == null || newJobDto.getCompanyName().isBlank()) {
			throw new AppException(21901, "Tên công ty không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (newJobDto.getPosition() == null || newJobDto.getPosition().isBlank()) {
			throw new AppException(21902, "Chức vụ không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<JobModel> optionalJob = jobRepository.findByUserIdAndCompanyNameAndPosition(userId,
				newJobDto.getCompanyName(), newJobDto.getPosition());
		JobModel newJob;
		if (optionalJob.isPresent()) {
			if (!optionalJob.get().getIsDelete()) {
				throw new AppException(21903, "Công việc đã tồn tại", HttpStatus.BAD_REQUEST);
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
			newJob.setUserId(userId);
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

	@PutMapping("profile/job/{jobId}")
	public ResponseEntity<String> updateJob(@RequestHeader String userId, @PathVariable String jobId,
			@RequestBody JobDto updatedJobDto) {

		Optional<JobModel> optionalJob = jobRepository.findByUserIdAndJobId(userId, jobId);
		if (optionalJob.isEmpty()) {
			throw new AppException(22000, "Không tìm thấy công việc", HttpStatus.NOT_FOUND);
		}

		JobModel jobToUpdate = optionalJob.get();

		Optional<JobModel> existingJob = jobRepository.findByUserIdAndCompanyNameAndPosition(userId,
				updatedJobDto.getCompanyName(), updatedJobDto.getPosition());
		if (existingJob.isPresent() && !existingJob.get().getJobId().equals(jobId)) {
			throw new AppException(22001, "Không thể cập nhật vì đã tồn tại công việc với cùng công ty và chức vụ",
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

	@GetMapping("/{id}/profile/education")
	public ResponseEntity<Map<String, Object>> getAllEducations(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22100, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		List<EducationModel> edus = educationRepository.findByUserId(id);

		HashMap<String, Object> result = new HashMap<>();
		result.put("education", edus);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}/profile/education/{educationId}")
	public ResponseEntity<IEducationDto> getEducationById(@PathVariable String id, @PathVariable String educationId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22200, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<IEducationDto> optionalEdu = educationRepository.findByEducationId(educationId);
		if (optionalEdu.isEmpty()) {
			throw new AppException(22201, "Không có bằng cấp nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalEdu.get());
	}

	@PostMapping("/profile/education")
	public ResponseEntity<String> createEducation(@RequestHeader String userId,
			@RequestBody EducationDto newEducationDto) {
		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new AppException(22300, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		if (newEducationDto.getSchoolName() == null || newEducationDto.getSchoolName().isBlank()) {
			throw new AppException(22301, "Tên trường học không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (newEducationDto.getDegree() == null || newEducationDto.getDegree().isBlank()) {
			throw new AppException(22302, "Tên bằng cấp không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<EducationModel> optionalEdu = educationRepository.findByUserIdAndSchoolNameAndDegree(userId,
				newEducationDto.getSchoolName(), newEducationDto.getDegree());
		EducationModel newEdu;
		if (optionalEdu.isPresent()) {
			if (!optionalEdu.get().getIsDelete()) {
				throw new AppException(22303, "Học vấn đã tồn tại", HttpStatus.BAD_REQUEST);
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
			newEdu.setUserId(userId);
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

	@PutMapping("/profile/education/{educationId}")
	public ResponseEntity<String> updateEducation(@RequestHeader String userId, @PathVariable String educationId,
			@RequestBody EducationDto updatedEducationDto) {

		Optional<EducationModel> optionalEdu = educationRepository.findByUserIdAndEducationId(userId, educationId);
		if (optionalEdu.isEmpty()) {
			throw new AppException(22400, "Không tìm thấy học vấn", HttpStatus.NOT_FOUND);
		}

		EducationModel eduToUpdate = optionalEdu.get();

		Optional<EducationModel> existingEdu = educationRepository.findByUserIdAndSchoolNameAndDegree(userId,
				updatedEducationDto.getSchoolName(), updatedEducationDto.getDegree());
		if (existingEdu.isPresent() && !existingEdu.get().getEducationId().equals(educationId)) {
			throw new AppException(22401, "Không thể cập nhật vì đã tồn tại học vấn với cùng trường học và bằng cấp",
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

	@GetMapping("/{id}/profile/achievement")
	public ResponseEntity<Map<String, Object>> getAllAchievements(@PathVariable String id) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22500, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		List<AchievementModel> achievements = achievementRepository.findByUserId(id);

		HashMap<String, Object> result = new HashMap<>();
		result.put("achievements", achievements);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}/profile/achievement/{achievementId}")
	public ResponseEntity<IAchievementDto> getAchievementById(@PathVariable String id,
			@PathVariable String achievementId) {
		Optional<UserModel> optionalUser = userRepository.findById(id);
		if (optionalUser.isEmpty()) {
			throw new AppException(22600, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<IAchievementDto> optionalAchievement = achievementRepository.findByAchievementId(achievementId);
		if (optionalAchievement.isEmpty()) {
			throw new AppException(22601, "Không có thành tựu nào được tìm thấy cho người dùng này",
					HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalAchievement.get());
	}

	@PostMapping("/profile/achievement")
	public ResponseEntity<String> createAchievement(@RequestHeader String userId,
			@RequestBody AchievementDto newAchievementDto) {
		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new AppException(22700, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		if (newAchievementDto.getAchievementName() == null || newAchievementDto.getAchievementName().isBlank()) {
			throw new AppException(22701, "Tên thành tựu không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (newAchievementDto.getAchievementType() == null || newAchievementDto.getAchievementType().isBlank()) {
			throw new AppException(22702, "Loại thành tựu không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<AchievementModel> optionalAchievement = achievementRepository
				.findByUserIdAndAchievementNameAndAchievementType(userId, newAchievementDto.getAchievementName(),
						newAchievementDto.getAchievementType());
		AchievementModel newAchievement;
		if (optionalAchievement.isPresent()) {
			if (!optionalAchievement.get().getIsDelete()) {
				throw new AppException(22703, "Thành tựu đã tồn tại", HttpStatus.BAD_REQUEST);
			} else {
				newAchievement = optionalAchievement.get();
				if (newAchievementDto.getAchievementTime() == null) {
					newAchievement.setAchievementTime(
							Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
				} else {
					newAchievement.setAchievementTime(newAchievementDto.getAchievementTime());
				}
				newAchievement.setPrivacy(Privacy.valueOf(newAchievementDto.getPrivacy().toUpperCase()));
				newAchievement.setIsDelete(false);
			}
		} else {
			newAchievement = new AchievementModel();
			newAchievement.setAchievementId(UUID.randomUUID().toString());
			newAchievement.setUserId(userId);
			newAchievement.setAchievementName(newAchievementDto.getAchievementName());
			newAchievement.setAchievementType(newAchievementDto.getAchievementType());
			if (newAchievementDto.getAchievementTime() == null) {
				newAchievement.setAchievementTime(
						Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} else {
				newAchievement.setAchievementTime(newAchievementDto.getAchievementTime());
			}
			newAchievement.setPrivacy(Privacy.valueOf(newAchievementDto.getPrivacy().toUpperCase()));
		}

		achievementRepository.save(newAchievement);

		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("/profile/achievement/{achievementId}")
	public ResponseEntity<String> updateAchievement(@RequestHeader String userId, @PathVariable String achievementId,
			@RequestBody AchievementDto updatedAchievementDto) {

		Optional<AchievementModel> optionalAchievement = achievementRepository.findByUserIdAndAchievementId(userId,
				achievementId);
		if (optionalAchievement.isEmpty()) {
			throw new AppException(22800, "Không tìm thấy thành tựu", HttpStatus.NOT_FOUND);
		}

		AchievementModel achievementToUpdate = optionalAchievement.get();

		Optional<AchievementModel> existingAchievement = achievementRepository
				.findByUserIdAndAchievementNameAndAchievementType(userId, updatedAchievementDto.getAchievementName(),
						updatedAchievementDto.getAchievementType());
		if (existingAchievement.isPresent() && !existingAchievement.get().getAchievementId().equals(achievementId)) {
			throw new AppException(22801,
					"Không thể cập nhật vì đã tồn tại thành tựu với cùng tên thành tựu và loại thành tựu",
					HttpStatus.BAD_REQUEST);
		}

		if (updatedAchievementDto.getAchievementName() != null
				|| !updatedAchievementDto.getAchievementName().isEmpty()) {
			achievementToUpdate.setAchievementName(updatedAchievementDto.getAchievementName());
		}
		if (updatedAchievementDto.getAchievementType() != null
				&& !updatedAchievementDto.getAchievementType().isEmpty()) {
			achievementToUpdate.setAchievementType(updatedAchievementDto.getAchievementType());
		}
		if (updatedAchievementDto.getAchievementTime() != null) {
			achievementToUpdate.setAchievementTime(updatedAchievementDto.getAchievementTime());
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

	@GetMapping("/friends/{id}")
	public ResponseEntity<Map<String, Object>> getFriends(@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "orderBy", required = false, defaultValue = "createAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "asc") String order,
			@RequestParam(value = "fullName", required = false) String fullName) {

		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}

		Map<String, Object> result = new HashMap<>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<IFriendDto> friendsPage;

			if (fullName == null || fullName.isEmpty()) {
				friendsPage = friendRepository.getAllUserFriends(id, pageable);
			} else {
				friendsPage = friendRepository.getUserFriendsByFullName(id, fullName, pageable);
			}

			result.put("totalPages", friendsPage.getTotalPages());
			result.put("friends", friendsPage.getContent());

			return ResponseEntity.status(HttpStatus.OK).body(result);

		} catch (IllegalArgumentException e) {
			throw new AppException(22900, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new AppException(22901, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/friends/count/{id}")
	public ResponseEntity<Long> getSearchFriendResultCount(@PathVariable String id) {
		try {
			Long count = friendRepository.countFriendByUserId(id);
			return ResponseEntity.status(HttpStatus.OK).body(count);
		} catch (Exception e) {
			throw new AppException(23000, "Lỗi khi lấy số lượng bạn bè. Vui lòng thử lại", HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/friends/{friendId}")
	public ResponseEntity<String> markAsUnfriend(@RequestHeader("userId") String userId,
			@PathVariable String friendId) {

		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new AppException(23100, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<FriendModel> optionalFriend = friendRepository.findByUserIdAndFriendIdAndIsDelete(userId, friendId);
		if (optionalFriend.isEmpty()) {
			throw new AppException(23101, "Không tìm thấy bạn bè", HttpStatus.NOT_FOUND);
		}
		FriendModel friend = optionalFriend.get();
		friendRepository.delete(friend);
		// Add Remove
//		Optional<FriendModel> optionalReverseFriend = friendRepository.findByUserIdAndFriendIdAndIsDelete(friendId, userId);
//	    if (optionalReverseFriend.isPresent()) {
//	        FriendModel reverseFriend = optionalReverseFriend.get();
//	        friendRepository.delete(reverseFriend);
//	    }

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PostMapping("/friends/requests")
	public ResponseEntity<String> sendFriendRequest(@RequestHeader("userId") String userId,
			@RequestBody FriendIdRequestDto friendRequestDTO) {

		String friendId = friendRequestDTO.getFriendId();

		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new AppException(23200, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}
		Optional<UserModel> optionalFriend = userRepository.findById(friendId);
		if (optionalFriend.isEmpty()) {
			throw new AppException(23201, "Không tìm thấy thông tin bạn bè", HttpStatus.NOT_FOUND);
		}
		UserModel user = optionalUser.get();
		UserModel friend = optionalFriend.get();
		Optional<FriendModel> existingFriend = friendRepository.findByUserIdAndFriendIdAndIsDelete(userId, friendId);
		if (existingFriend.isPresent() && !existingFriend.get().getIsDelete()) {
			throw new AppException(23202, "Đã là bạn bè của nhau", HttpStatus.CONFLICT);
		}

		Optional<FriendRequestModel> existingFriendRequest = friendRequestRepository.findByUserIdAndFriendId(userId,
				friendId);
		if (existingFriendRequest.isPresent() && !existingFriendRequest.get().getIsDelete()) {
			throw new AppException(23203, "Đã hủy lời mời", HttpStatus.CONFLICT);
		}

		FriendRequestModel friendRequest = new FriendRequestModel();
		FriendRequestId friendRequestId = new FriendRequestId(userId, friendId);
		friendRequest.setId(friendRequestId);
		friendRequest.setUser(user);
		friendRequest.setFriend(friend);
		friendRequest.setIsDelete(false);

		friendRequestRepository.save(friendRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("/friends/requests")
	public ResponseEntity<String> handleFriendRequest(@RequestHeader("userId") String userId,
			@RequestBody FriendRequestActionDTO requestActionDTO) {
		String friendId = requestActionDTO.getFriendId();
		FriendRequestAction action = requestActionDTO.getAction();

		Optional<UserModel> optionalUser = userRepository.findById(userId);
		if (optionalUser.isEmpty()) {
			throw new AppException(23300, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND);
		}

		Optional<UserModel> optionalFriend = userRepository.findById(friendId);
		if (optionalFriend.isEmpty()) {
			throw new AppException(23301, "Không tìm thấy thông tin bạn bè", HttpStatus.NOT_FOUND);
		}

		if (action == FriendRequestAction.ACCEPT) {
			Optional<FriendRequestModel> optionalFriendRequest = friendRequestRepository
					.findByUserIdAndFriendIdAndIsDelete(friendId, userId);
			if (optionalFriendRequest.isEmpty()) {
				throw new AppException(23302, "Không tìm thấy lời mời kết bạn", HttpStatus.NOT_FOUND);
			}
			FriendRequestModel friendRequest = optionalFriendRequest.get();
			friendRequestRepository.delete(friendRequest);

			UserModel user = optionalUser.get();
			UserModel friend = optionalFriend.get();

			FriendId friendIdObj = new FriendId(userId, friendId);
			FriendModel friendModel = new FriendModel();
			friendModel.setId(friendIdObj);
			friendModel.setUser(user);
			friendModel.setFriend(friend);
			friendModel.setIsDelete(false);
			friendRepository.save(friendModel);

			FriendId reverseFriendIdObj = new FriendId(friendId, userId);
			FriendModel reverseFriendModel = new FriendModel();
			reverseFriendModel.setId(reverseFriendIdObj);
			reverseFriendModel.setUser(friend);
			reverseFriendModel.setFriend(user);
			reverseFriendModel.setIsDelete(false);
			friendRepository.save(reverseFriendModel);

			return ResponseEntity.status(HttpStatus.OK).body("");
		} else if (action == FriendRequestAction.DENY) {
			Optional<FriendRequestModel> existingFriendRequest = friendRequestRepository.findByUserIdAndFriendId(userId,
					friendId);
			if (existingFriendRequest.isEmpty() || existingFriendRequest.get().getIsDelete()) {
				throw new AppException(23303, "Không tìm thấy lời mời kết bạn", HttpStatus.NOT_FOUND);
			}

			FriendRequestModel friendRequest = existingFriendRequest.get();
			friendRequest.setIsDelete(true);
			friendRequestRepository.save(friendRequest);

			return ResponseEntity.status(HttpStatus.OK).body("");
		} else {
			throw new AppException(23304, "Hành động không hợp lệ", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/friends/requests")
	public ResponseEntity<Map<String, Object>> getAllFriendRequests(@RequestHeader("userId") String userId,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
			@RequestParam(value = "orderBy", defaultValue = "createAt") String orderBy,
			@RequestParam(value = "order", defaultValue = "desc") String order) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}

		Map<String, Object> result = new HashMap<>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<IFriendRequestDto> friendRequestsPage = friendRequestRepository.findByFriendIdAndIsDelete(userId,
					pageable);

			result.put("totalPages", friendRequestsPage.getTotalPages());
			result.put("friendRequests", friendRequestsPage.getContent());

			return ResponseEntity.status(HttpStatus.OK).body(result);

		} catch (IllegalArgumentException e) {
			throw new AppException(23400, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new AppException(23401, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/suggestion")
	public ResponseEntity<HashMap<String, Object>> getAllSuggestionUsers(@RequestHeader("userId") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "orderBy", required = false, defaultValue = "full_name") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "asc") String order,
			@RequestParam(value = "fullName", required = false) String fullName,
			@RequestParam(value = "email", required = false) String email) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<ISuggestionUserDto> users = null;

			users = userRepository.getSuggestionUsers(fullName, email, userId, pageable);

			result.put("totalPages", users.getTotalPages());
			result.put("users", users.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(21000, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new AppException(21001, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

}