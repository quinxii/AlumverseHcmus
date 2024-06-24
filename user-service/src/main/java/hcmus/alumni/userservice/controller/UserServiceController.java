package hcmus.alumni.userservice.controller;

import java.io.IOException;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import hcmus.alumni.userservice.config.UserConfig;
import hcmus.alumni.userservice.dto.UserSearchDto;
import hcmus.alumni.userservice.dto.VerifyAlumniDto;
import hcmus.alumni.userservice.exception.AppException;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.PasswordHistoryModel;
import hcmus.alumni.userservice.model.RoleModel;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.repository.PasswordHistoryRepository;
import hcmus.alumni.userservice.repository.RoleRepository;
import hcmus.alumni.userservice.repository.StatusUserGroupRepository;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.repository.VerifyAlumniRepository;
import hcmus.alumni.userservice.utils.EmailSenderUtils;
import hcmus.alumni.userservice.utils.ImageUtils;

@RestController
@CrossOrigin(origins = "http://localhost:3000") // Allow requests from Web
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
			@RequestParam(value = "roleId", required = false) List<Integer> roleIds) {

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
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "fullName", required = false) String fullName,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "roleIds", required = false) List<Integer> roleIds) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize);
			Page<UserSearchDto> searchResult = null;

            searchResult = userRepository.searchUsers(fullName, email, roleIds, pageable);

			result.put("totalPages", searchResult.getTotalPages());
			result.put("users", searchResult.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(21001, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			throw new AppException(21002, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}