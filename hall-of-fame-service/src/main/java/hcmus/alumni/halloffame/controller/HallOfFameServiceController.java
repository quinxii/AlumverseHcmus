package hcmus.alumni.halloffame.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import hcmus.alumni.halloffame.dto.IHallOfFameDto;
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
@CrossOrigin(origins = "http://localhost:3000")
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

	@GetMapping("/count")
	public ResponseEntity<Long> getHofCount(@RequestParam(value = "status") String status) {
		if (status.equals("")) {
			ResponseEntity.status(HttpStatus.OK).body(0L);
		}
		return ResponseEntity.status(HttpStatus.OK).body(halloffameRepository.getCountByStatus(status));
	}

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getHof(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "facultyId", required = false) Integer facultyId,
			@RequestParam(value = "beginningYear", required = false) Integer beginningYear) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<IHallOfFameDto> hof = null;

			hof = halloffameRepository.searchHof(title, statusId, facultyId, beginningYear, pageable);

			result.put("totalPages", hof.getTotalPages());
			result.put("hof", hof.getContent());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			result.put("error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@PostMapping("")
	public ResponseEntity<String> createHallOfFame(@RequestHeader("userId") String creator,
			@RequestParam(value = "title") String title, @RequestParam(value = "thumbnail") MultipartFile thumbnail,
			@RequestParam(value = "summary", required = false) String summary,
			@RequestParam(value = "facultyId", defaultValue = "0") Integer facultyId,
			@RequestParam(value = "emailOfUser", required = false) String emailOfUser,
			@RequestParam(value = "beginningYear", required = false) Integer beginningYear,
			@RequestParam(value = "scheduledTime", required = false) Long scheduledTimeMili,
			@RequestParam(value = "position", required = false) String position) {
		if (creator.isEmpty() || title.isEmpty() || thumbnail.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Title and thumbnail must not be empty");
		}
		if (thumbnail.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
		}

		UserModel linkedUser = null;
		if (emailOfUser != null) {
			linkedUser = userRepository.findByEmail(emailOfUser);
			if (linkedUser == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not existed");
			}
		}

		String id = UUID.randomUUID().toString();
		try {
			// Save thumbnail image
			String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getHofPath(id), thumbnail, "thumbnail");
			// Save hall of fame to database
			FacultyModel faculty = null;
			if (!facultyId.equals(0)) {
				faculty = new FacultyModel(facultyId);
			}
			HallOfFameModel halloffame = new HallOfFameModel(id, new UserModel(creator), title, thumbnailUrl, summary,
					faculty, linkedUser, beginningYear, scheduledTimeMili, position);

			halloffameRepository.save(halloffame);
		} catch (IOException e) {
			System.err.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save thumbnail");
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
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
			// Find hall of fame
			Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
			if (optionalHallOfFame.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
			}

			HallOfFameModel halloffame = optionalHallOfFame.get();
			boolean isPut = false;

			// Update fields if provided
			if (!title.equals("")) {
				halloffame.setTitle(title);
				isPut = true;
			}
			if (thumbnail != null && !thumbnail.isEmpty()) {
				// Save new thumbnail
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
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not existed");
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

			// Save hall of fame
			if (isPut) {
				halloffameRepository.save(halloffame);
			}
			return ResponseEntity.status(HttpStatus.OK).body("Updated successfully!");

		} catch (IOException e) {
			System.err.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update hall of fame");
		}

	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@PutMapping("/{id}/content")
	public ResponseEntity<String> updateHallOfFameContent(@PathVariable String id,
			@RequestBody(required = false) HallOfFameModel updatedHallOfFame) {
		if (updatedHallOfFame.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}
		// Find hof
		Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
		if (optionalHallOfFame.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
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

	@PreAuthorize("hasAnyAuthority('Admin')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteHallOfFame(@PathVariable String id) {
		Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
		if (optionalHallOfFame.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
		}
		HallOfFameModel hof = optionalHallOfFame.get();
		hof.setStatus(new StatusPostModel(4));
		halloffameRepository.save(hof);
		return ResponseEntity.status(HttpStatus.OK).body("Updated status successfully!");
	}

	@GetMapping("/{id}")
	public ResponseEntity<IHallOfFameDto> getHallOfFameById(@PathVariable String id) {
		Optional<IHallOfFameDto> optionalHof = halloffameRepository.findHallOfFameById(id);
		if (optionalHof.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		halloffameRepository.viewsIncrement(id);
		return ResponseEntity.status(HttpStatus.OK).body(optionalHof.get());
	}
}