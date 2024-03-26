package hcmus.alumni.userservice.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import hcmus.alumni.userservice.dto.IVerifyAlumniDto;
import hcmus.alumni.userservice.dto.VerifyAlumniDto;
import hcmus.alumni.userservice.model.FacultyModel;
import hcmus.alumni.userservice.model.UserModel;
import hcmus.alumni.userservice.model.VerifyAlumniModel;
import hcmus.alumni.userservice.repository.UserRepository;
import hcmus.alumni.userservice.repository.VerifyAlumniRepository;
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
	private VerifyAlumniRepository verifyAlumniRepository;

	@Autowired
	private ImageUtils imageUtils;

	@GetMapping("/alumni-verification/count")
	public ResponseEntity<Long> getPendingAlumniVerificationCount(@RequestParam String status) {
		switch (status) {
		case "pending":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusEquals(false, VerifyAlumniModel.Status.PENDING));
		case "resolved":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusNot(false, VerifyAlumniModel.Status.PENDING));
		case "approved":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusEquals(false, VerifyAlumniModel.Status.APPROVED));
		case "denied":
			return ResponseEntity.status(HttpStatus.OK).body(verifyAlumniRepository
					.countByIsDeleteEqualsAndStatusEquals(false, VerifyAlumniModel.Status.DENIED));
		default:
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	@GetMapping("/alumni-verification")
	public ResponseEntity<HashMap<String, Object>> searchAlumniVerification(@RequestParam String status,
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "criteria", required = false, defaultValue = "email") String criteria,
			@RequestParam(value = "createAtOrder", required = false, defaultValue = "desc") String createAtOrder,
			@RequestParam(value = "studentIdOrder", required = false, defaultValue = "") String studentIdOrder,
			@RequestParam(value = "fullNameOrder", required = false, defaultValue = "") String fullNameOrder,
			@RequestParam(value = "beginningYearOrder", required = false, defaultValue = "") String beginningYearOrder) {

		// Initiate
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VerifyAlumniDto> cq = cb.createQuery(VerifyAlumniDto.class);
		// From
		Root<VerifyAlumniModel> root = cq.from(VerifyAlumniModel.class);

		// Join
		Join<VerifyAlumniModel, UserModel> userJoin = root.join("user", JoinType.INNER);
		Join<VerifyAlumniModel, FacultyModel>facultyJoin = root.join("faculty", JoinType.LEFT);

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
				commentSelection, statusSelection, emailSelection, fullNameSelection, avatarUrlSelection, facultyNameSelection);

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
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		Predicate isDeletePredicate = cb.equal(root.get("isDelete"), false);
		Predicate criteriaPredicate = null;
		if (criteria.equals("email") || criteria.equals("fullName")) {
			criteriaPredicate = cb.like(userJoin.get(criteria), "%" + keyword + "%");
		} else if (criteria.equals("studentId") || criteria.equals("beginningYear")) {
			criteriaPredicate = cb.like(root.get(criteria), "%" + keyword + "%");
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		cq.where(statusPredication, criteriaPredicate, isDeletePredicate);

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

	@GetMapping("/alumni-verification/{user_id}")
	public ResponseEntity<Map<String, Object>> getAlumniVerificationByUserId(@PathVariable String user_id) {
		Optional<VerifyAlumniModel> alumniVerificationOptional = verifyAlumniRepository
				.findByUserIdAndIsDeleteEquals(user_id, false);

		if (alumniVerificationOptional.isEmpty()) {
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "No alumni verification with that userid");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
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

	@PostMapping("/alumni-verification")
	public ResponseEntity<String> createAlumniVerification(@RequestHeader("userId") String userId,
			@RequestParam(value = "avatar", required = false) MultipartFile avatar,
			@RequestParam("fullName") String fullName,
			@RequestParam(value = "studentId", required = false) String studentId,
			@RequestParam(value = "beginningYear", required = false, defaultValue = "0") Integer beginningYear,
			@RequestParam(value = "socialMediaLink", required = false) String socialMediaLink,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId) {
		if (avatar != null && avatar.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
		}
		
		UserModel user = new UserModel();
		user.setId(userId);
		FacultyModel faculty = new FacultyModel();
		if (beginningYear.equals(0)) {
			beginningYear = null;
		}
		if (facultyId.equals(0)) {
			faculty = null;
		} else {
			faculty.setId(facultyId);
		}
		VerifyAlumniModel verifyAlumni = new VerifyAlumniModel(user, studentId, beginningYear, socialMediaLink, faculty);

		try {
			userRepository.setFullName(userId, fullName);
			if (studentId != null || beginningYear != 0 || socialMediaLink != null) {
				verifyAlumniRepository.save(verifyAlumni);
			}

			// Delete old avatar if users update theirs
			// String oldAvatarUrl = userRepository.getAvatarUrl(userID);
			// imageUtils.deleteImageFromStorageByUrl(oldAvatarUrl);

			// Save avatar
			String imageName = avatar == null ? null : ImageUtils.hashImageName(userId);
			String avatarUrl = imageUtils.saveImageToStorage(imageUtils.getAvatarPath(), avatar, imageName);
			userRepository.setAvatarUrl(userId, avatarUrl);
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.CREATED).body("Post alumni verification successfully");
	}

	@PutMapping("/alumni-verification/{user_id}")
	public ResponseEntity<String> updateAlumniVerification(@PathVariable String user_id,
			@RequestBody Map<String, Object> requestBody) {

		Optional<VerifyAlumniModel> optionalAlumni = verifyAlumniRepository.findByUserIdAndIsDeleteEquals(user_id,
				false);

		if (optionalAlumni.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		VerifyAlumniModel alumni = optionalAlumni.get();

		try {
			if (requestBody.containsKey("fullName")) {
				String fullName = (String) requestBody.get("fullName");
				userRepository.setFullName(user_id, fullName);
			}
			if (requestBody.containsKey("studentId")) {
				String studentId = (String) requestBody.get("studentId");
				alumni.setStudentId(studentId);
			}
			if (requestBody.containsKey("beginningYear")) {
				Integer beginningYear = (Integer) requestBody.get("beginningYear");
				alumni.setBeginningYear(beginningYear);
			}
			if (requestBody.containsKey("socialMediaLink")) {
				String socialMediaLink = (String) requestBody.get("socialMediaLink");
				alumni.setSocialMediaLink(socialMediaLink);
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

	@PutMapping("/alumni-verification/{id}/verify")
	public ResponseEntity<String> updateAlumniVerificationStatus(@PathVariable String id,
			@RequestBody Map<String, Object> requestBody) {
		VerifyAlumniModel va = verifyAlumniRepository.findByIdAndIsDeleteEquals(id, false);
		if (va == null) {
			return ResponseEntity.notFound().build();
		}

		String status = (String) requestBody.get("status");
		String comment = (String) requestBody.get("comment");
		try {
			VerifyAlumniModel.Status temp = VerifyAlumniModel.Status.valueOf(status);
			if (temp == VerifyAlumniModel.Status.PENDING) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status is invalid");
			}
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Status is invalid");
		}

		va.setStatus(VerifyAlumniModel.Status.valueOf(status));
		va.setComment(comment);
		verifyAlumniRepository.save(va);
		return ResponseEntity.ok("Alumni verification approved successfully");
	}
}
