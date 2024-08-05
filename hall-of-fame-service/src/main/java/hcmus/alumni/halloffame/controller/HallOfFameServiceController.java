package hcmus.alumni.halloffame.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import hcmus.alumni.halloffame.common.FetchHofMode;
import hcmus.alumni.halloffame.dto.IHallOfFameDto;
import hcmus.alumni.halloffame.dto.IHallOfFameListDto;
import hcmus.alumni.halloffame.exception.AppException;
import hcmus.alumni.halloffame.model.FacultyModel;
import hcmus.alumni.halloffame.model.HallOfFameModel;
import hcmus.alumni.halloffame.model.StatusPostModel;
import hcmus.alumni.halloffame.model.UserModel;
import hcmus.alumni.halloffame.repository.HallOfFameRepository;
import hcmus.alumni.halloffame.repository.UserRepository;
import hcmus.alumni.halloffame.utils.ImageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/hof")
public class HallOfFameServiceController {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private HallOfFameRepository halloffameRepository;
	@Autowired
	private UserRepository userRepository;;
	@Autowired
	private ImageUtils imageUtils;

	private final static int ADMIN_ROLE_ID = 1;
	private final static int FACULTY_MANAGER_ROLE_ID = 2;

	@GetMapping("/count")
	public ResponseEntity<Long> getHofCount(@RequestParam(value = "status") String status) {
		if (status.equals(StringUtils.EMPTY)) {
			ResponseEntity.status(HttpStatus.OK).body(0L);
		}
		return ResponseEntity.status(HttpStatus.OK).body(halloffameRepository.getCountByStatus(status));
	}

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getHof(
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "facultyId", required = false) Integer facultyId,
			@RequestParam(value = "beginningYear", required = false) Integer beginningYear,
			@RequestParam(value = "fetchMode", required = false, defaultValue = "NORMAL") FetchHofMode fetchMode) {
		if (pageSize <= 0 || pageSize > 50) {
			pageSize = 50;
		}
		if (title.isBlank()) {
			title = null;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<IHallOfFameListDto> hof = null;

			if (fetchMode.equals(FetchHofMode.MANAGEMENT)) {
				// Check if is Admin or FacultyManager
				Set<Integer> roleIds = userRepository.getRoleIds(userId);
				if (roleIds.contains(FACULTY_MANAGER_ROLE_ID)) {
					hof = halloffameRepository.searchHofByUserFaculty(userId, title, statusId, beginningYear, pageable);
				}
			}

			if (hof == null) {
				hof = halloffameRepository.searchHof(title, statusId, facultyId, beginningYear, pageable);
			}

			result.put("totalPages", hof.getTotalPages());
			result.put("hof", hof.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(30200, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(30201, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<IHallOfFameDto> getHallOfFameById(@PathVariable String id) {
		Optional<IHallOfFameDto> optionalHof = halloffameRepository.findHallOfFameById(id);
		if (optionalHof.isEmpty()) {
			throw new AppException(30300, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		halloffameRepository.viewsIncrement(id);
		return ResponseEntity.status(HttpStatus.OK).body(optionalHof.get());
	}

	@PreAuthorize("hasAnyAuthority('Hof.Create')")
	@PostMapping("")
	public ResponseEntity<String> createHallOfFame(@RequestHeader("userId") String creator,
			@RequestParam(value = "title") String title, @RequestParam(value = "thumbnail") MultipartFile thumbnail,
			@RequestParam(value = "summary", required = false) String summary,
			@RequestParam(value = "facultyId", defaultValue = "0") Integer facultyId,
			@RequestParam(value = "emailOfUser", required = false) String emailOfUser,
			@RequestParam(value = "beginningYear", required = false) Integer beginningYear,
			@RequestParam(value = "scheduledTime", required = false) Long scheduledTimeMili,
			@RequestParam(value = "position", required = false) String position) {
		if (title.isEmpty()) {
			throw new AppException(30400, "Tiêu đề không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (thumbnail.isEmpty()) {
			throw new AppException(30401, "Ảnh thumbnail không được để trống", HttpStatus.BAD_REQUEST);
		}

		String id = UUID.randomUUID().toString();

		UserModel linkedUser = null;
		if (emailOfUser != null) {
			linkedUser = userRepository.findByEmail(emailOfUser);
			if (linkedUser == null) {
				throw new AppException(30402, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
			}
		}

		try {
			String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getHofPath(id), thumbnail, "thumbnail");
			FacultyModel faculty = null;
			if (!facultyId.equals(0)) {
				faculty = new FacultyModel(facultyId);
			}
			HallOfFameModel halloffame = new HallOfFameModel(id, new UserModel(creator), title, thumbnailUrl, summary,
					faculty, linkedUser, beginningYear, scheduledTimeMili, position);

			halloffameRepository.save(halloffame);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(30403, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('Hof.Edit')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateHallOfFame(@PathVariable String id,
			@RequestParam(value = "title", defaultValue = "") String title,
			@RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
			@RequestParam(value = "summary", defaultValue = "") String summary,
			@RequestParam(value = "facultyId", defaultValue = "0") Integer facultyId,
			@RequestParam(value = "emailOfUser", required = false) String emailOfUser,
			@RequestParam(value = "beginningYear", required = false) Integer beginningYear,
			@RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId,
			@RequestParam(value = "position", defaultValue = "") String position) {

		try {
			Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
			if (optionalHallOfFame.isEmpty()) {
				throw new AppException(30500, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
			}

			HallOfFameModel halloffame = optionalHallOfFame.get();
			boolean isPut = false;

			if (!title.equals("")) {
				halloffame.setTitle(title);
				isPut = true;
			}
			if (thumbnail != null && !thumbnail.isEmpty()) {
				imageUtils.saveImageToStorage(imageUtils.getHofPath(id), thumbnail, "thumbnail");
			}
			if (!summary.equals("")) {
				halloffame.setSummary(summary);
				isPut = true;
			}
			if (!facultyId.equals(0)) {
				halloffame.setFaculty(new FacultyModel(facultyId));
				isPut = true;
			}
			if (beginningYear != null) {
				halloffame.setBeginningYear(beginningYear);
				isPut = true;
			}

			if (emailOfUser != null) {
				UserModel linkedUser = userRepository.findByEmail(emailOfUser);
				if (linkedUser == null) {
					throw new AppException(30501, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND);
				}
				halloffame.setLinkedUser(linkedUser);
				isPut = true;
			}
			if (!statusId.equals(0)) {
				halloffame.setStatus(new StatusPostModel(statusId));
				isPut = true;
			}

			if (!position.equals("")) {
				halloffame.setPosition(position);
				isPut = true;
			}

			if (isPut) {
				halloffameRepository.save(halloffame);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(30502, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('Hof.Edit')")
	@PutMapping("/{id}/content")
	public ResponseEntity<String> updateHallOfFameContent(@PathVariable String id,
			@RequestBody(required = false) HallOfFameModel updatedHallOfFame) {
		if (updatedHallOfFame.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}

		Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
		if (optionalHallOfFame.isEmpty()) {
			throw new AppException(30600, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		HallOfFameModel halloffame = optionalHallOfFame.get();
		if (!updatedHallOfFame.getContent().equals("")) {
			String newContent = imageUtils.updateImgFromHtmlToStorage(halloffame.getContent(),
					updatedHallOfFame.getContent(), id);
			if (newContent.equals(halloffame.getContent())) {
				return ResponseEntity.status(HttpStatus.OK).body("");
			}
			halloffame.setContent(newContent);
			halloffameRepository.save(halloffame);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('Hof.Delete')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteHallOfFame(@PathVariable String id) {
		Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
		if (optionalHallOfFame.isEmpty()) {
			throw new AppException(30700, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		HallOfFameModel hof = optionalHallOfFame.get();
		hof.setStatus(new StatusPostModel(4));
		halloffameRepository.save(hof);
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("/rand")
	public ResponseEntity<HashMap<String, Object>> getRandomHof(
			@RequestParam(value = "number", defaultValue = "8") Integer number) {
		List<IHallOfFameListDto> optionalHallOfFame = halloffameRepository.findRandomHofEntries(number);

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("hof", optionalHallOfFame);

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}