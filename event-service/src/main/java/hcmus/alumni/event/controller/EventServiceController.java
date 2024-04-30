package hcmus.alumni.event.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import hcmus.alumni.event.dto.ICommentEventDto;
import hcmus.alumni.event.dto.IEventDto;
import hcmus.alumni.event.dto.IParticipantEventDto;
import hcmus.alumni.event.model.EventModel;
import hcmus.alumni.event.model.StatusPostModel;
import hcmus.alumni.event.model.FacultyModel;
import hcmus.alumni.event.model.ParticipantEventId;
import hcmus.alumni.event.model.ParticipantEventModel;
import hcmus.alumni.event.model.UserModel;
import hcmus.alumni.event.model.CommentEventModel;
import hcmus.alumni.event.repository.CommentEventRepository;
import hcmus.alumni.event.repository.EventRepository;
import hcmus.alumni.event.repository.ParticipantEventRepository;
import hcmus.alumni.event.utils.ImageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/events")
public class EventServiceController {
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private ParticipantEventRepository participantEventRepository;
	@Autowired
	private CommentEventRepository commentEventRepository;
	@Autowired
    private ImageUtils imageUtils;
	
	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getEvents(
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "title", required = false, defaultValue = "") String title,
	        @RequestParam(value = "orderBy", required = false, defaultValue = "organizationTime") String orderBy,
	        @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
	        @RequestParam(value = "facultyId", required = false) Integer facultyId,
	        @RequestParam(value = "tagsId", required = false) List<Integer> tagsId,
	        @RequestParam(value = "statusId", required = false) Integer statusId,
	        @RequestParam(value = "mode", required = false, defaultValue = "1") int mode) {
	    if (pageSize == 0 || pageSize > 50) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	    HashMap<String, Object> result = new HashMap<>();

	    try {
	        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
	        Page<IEventDto> events = null;
	        
	        Calendar cal = Calendar.getInstance();
	        Date startDate = cal.getTime();
	        events = eventRepository.searchEvents(title, statusId, facultyId, tagsId, startDate, mode, pageable);

	        result.put("totalPages", events.getTotalPages());
	        result.put("events", events.getContent());
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<IEventDto> getEventById(@PathVariable String id) {
	    Optional<IEventDto> optionalEvent = eventRepository.findEventById(id);
	    if (optionalEvent.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    }
	    eventRepository.incrementEventViews(id);
	    return ResponseEntity.status(HttpStatus.OK).body(optionalEvent.get());
	}
    
	@PreAuthorize("hasAnyAuthority('Admin')")
	@PostMapping("")
	public ResponseEntity<String> createEvent(
			@RequestHeader("userId") String creatorId,
	        @RequestParam(value = "title") String title, 
	        @RequestParam(value = "thumbnail") MultipartFile thumbnail,
	        @RequestParam(value = "content", required = false, defaultValue = "") String content,
	        @RequestParam(value = "organizationLocation", required = false, defaultValue = "") String organizationLocation,
	        @RequestParam(value = "organizationTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date organizationTime,
	        @RequestParam(value = "tagsId[]", required = false, defaultValue = "") Integer[] tagsId,
	        @RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
	        @RequestParam(value = "minimumParticipants", required = false, defaultValue = "0") Integer minimumParticipants,
	        @RequestParam(value = "maximumParticipants", required = false, defaultValue = "0") Integer maximumParticipants,
	        @RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId) {
	    if (creatorId.isEmpty() || title.isEmpty() || thumbnail.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields must not be empty");
	    }
	    if (thumbnail.getSize() > 5 * 1024 * 1024) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
	    }
	    String id = UUID.randomUUID().toString();

	    try {
	        // Save thumbnail image
	        String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getEventPath(id), thumbnail);
	        // Save event to database
	        EventModel event = new EventModel();
	        event.setId(id);
	        event.setCreator(new UserModel(creatorId));
	        event.setTitle(title);
	        event.setThumbnail(thumbnailUrl);
	        event.setContent(content);
	        event.setOrganizationLocation(organizationLocation);
	        event.setOrganizationTime(organizationTime);
	        event.setPublishedAt(new Date());
	        if (tagsId != null) {
	            event.setTags(tagsId);
	        }
	        if (!facultyId.equals(0)) {
	            event.setFaculty(new FacultyModel(facultyId));
	        }
	        event.setMinimumParticipants(minimumParticipants);
	        event.setMaximumParticipants(maximumParticipants);
	        event.setStatus(new StatusPostModel(statusId));
	        eventRepository.save(event);
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        System.err.println(e);
	    }
	    return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateEvent(@PathVariable String id,
	        @RequestParam(value = "title", defaultValue = "") String title,
	        @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
	        @RequestParam(value = "content", required = false, defaultValue = "") String content,
	        @RequestParam(value = "organizationLocation", required = false, defaultValue = "") String organizationLocation,
	        @RequestParam(value = "organizationTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date organizationTime,
	        @RequestParam(value = "tagsId[]", required = false, defaultValue = "") Integer[] tagsId,
	        @RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
	        @RequestParam(value = "minimumParticipants", required = false, defaultValue = "0") Integer minimumParticipants,
	        @RequestParam(value = "maximumParticipants", required = false, defaultValue = "0") Integer maximumParticipants,
	        @RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId) {
	    if (thumbnail != null && thumbnail.getSize() > 5 * 1024 * 1024) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
	    }
	    boolean isPut = false;

	    try {
	        // Find event
	        Optional<EventModel> optionalEvent = eventRepository.findById(id);
	        if (optionalEvent.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
	        }
	        EventModel event = optionalEvent.get();
	        if (thumbnail != null && !thumbnail.isEmpty()) {
	            imageUtils.saveImageToStorage(imageUtils.getEventPath(id), thumbnail);
	        }
	        if (!title.equals("")) {
	            event.setTitle(title);
	            isPut = true;
	        }
	        if (!content.equals("")) {
	            event.setContent(content);
	            isPut = true;
	        }
	        if (!organizationLocation.equals("")) {
	            event.setOrganizationLocation(organizationLocation);
	            isPut = true;
	        }
	        if (organizationTime != null) {
	            event.setOrganizationTime(organizationTime);
	            isPut = true;
	        }
	        if (tagsId != null) {
	            event.setTags(tagsId);
	            isPut = true;
	        }
	        if (!facultyId.equals(0)) {
	            event.setFaculty(new FacultyModel(facultyId));
	            isPut = true;
	        }
	        if (minimumParticipants != null) {
	        	event.setMinimumParticipants(minimumParticipants);
	            isPut = true;
	        }
	        if (maximumParticipants != null) {
	        	event.setMaximumParticipants(maximumParticipants);
	            isPut = true;
	        }
	        if (!statusId.equals(0)) {
	            event.setStatus(new StatusPostModel(statusId));
	            isPut = true;
	        }
	        if (isPut) {
	            eventRepository.save(event);
	        }
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        System.err.println(e);
	    }
	    return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteEvent(@PathVariable String id) {
	    // Find event
	    Optional<EventModel> optionalEvent = eventRepository.findById(id);
	    if (optionalEvent.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
	    }
	    EventModel event = optionalEvent.get();
	    event.setStatus(new StatusPostModel(4));
	    eventRepository.save(event);
	    return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("/hot")
	public ResponseEntity<HashMap<String, Object>> getHotEvents(
	        @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
	    if (limit <= 0 || limit > 5) {
	        limit = 5;
	    }
	    Calendar cal = Calendar.getInstance();
	    Date startDate = cal.getTime();

	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "participants"));
	    Page<IEventDto> events = eventRepository.getHotEvents(startDate, pageable);

	    HashMap<String, Object> result = new HashMap<>();
	    result.put("events", events.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/participated")
	public ResponseEntity<HashMap<String, Object>> getUserParticipatedEvents(
	        @RequestHeader("userId") String userId,
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "mode", required = false, defaultValue = "1") int mode) {
	    if (pageSize == 0 || pageSize > 50) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	    HashMap<String, Object> result = new HashMap<>();

	    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "organizationTime"));
	    Calendar cal = Calendar.getInstance();
        Date startDate = cal.getTime();
        Page<IEventDto> events = eventRepository.getUserParticipatedEvents(userId, startDate, mode, pageable);

        result.put("totalPages", events.getTotalPages());
        result.put("events", events.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/is-participated")
	public ResponseEntity<List<Object>> checkParticipated(
	        @RequestHeader("userId") String userId,
	        @RequestParam List<String> eventIds) {
		List<Object> resultList = new ArrayList<>();
	    for (String eventId : eventIds) {
			boolean isParticipated = !participantEventRepository.findById(new ParticipantEventId(eventId, userId))
			.filter(participantEventModel -> !participantEventModel.isDelete())
			.isEmpty();
			Map<String, Object> resultObject = new HashMap<>();
			resultObject.put("eventId", eventId);
			resultObject.put("isParticipated", isParticipated);
			resultList.add(resultObject);
	    }
	    return ResponseEntity.ok(resultList);
	}
	
	@GetMapping("/{id}/participants")
	public ResponseEntity<Map<String, Object>> getParticipantsListById(@PathVariable String id,
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
	    if (pageSize == 0 || pageSize > 50) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	    Map<String, Object> result = new HashMap<>();

	    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
	    Page<IParticipantEventDto> participantsPage = participantEventRepository.getParticipantsByEventId(id, pageable);

	    result.put("participants", participantsPage.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@PostMapping("/{id}/participants")
	public ResponseEntity<String> addParticipant(
	        @PathVariable String id,
	        @RequestHeader("userId") String userId,
	        @RequestBody ParticipantEventModel participantEvent) {
		Optional<IEventDto> optionalEvent = eventRepository.findEventById(id);
		if (optionalEvent.isEmpty()) 
		    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		if (optionalEvent.get().getParticipants() >= optionalEvent.get().getMaximumParticipants()) 
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số lượng người tham gia đã đạt tối đa!");
		
	    ParticipantEventModel participant = participantEvent;
	    participant.setId(new ParticipantEventId(id, userId));
	    participantEventRepository.save(participant);
	    //db còn cái trigger cho participant nên tui tạm phong ấn thằng này
//	    eventRepository.participantCountIncrement(id, 1);

	    return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@DeleteMapping("/{id}/participants")
	public ResponseEntity<String> deleteParticipant(
	        @PathVariable String id,
	        @RequestHeader("userId") String userId) {
	    // Delete the participant
	    participantEventRepository.deleteByEventIdAndUserId(id, userId);

	    // Update participant count for the event
	    eventRepository.participantCountIncrement(id, -1);

	    return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	@GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getNewsComments(@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentEventDto> comments = commentEventRepository.getComments(id, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getNewsChildrenComments(
			@PathVariable String commentId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentEventModel> parentComment = commentEventRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentEventDto> comments = commentEventRepository.getChildrenComment(commentId, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<CommentEventModel> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentEventModel comment) {
		comment.setId(UUID.randomUUID().toString());
		comment.setEvent(new EventModel(id));
		comment.setCreator(new UserModel(creator));
		commentEventRepository.save(comment);
		
		//Hiện tại children_comment_number của comment chỉ dùng để đếm comment con trực tiếp thôi, chứ không đếm comment cháu chắt
		//Còn nếu ông muốn đếm cả comment cháu chắt thì hú tui
		if (comment.getParentId() != null) {
	        commentEventRepository.commentCountIncrement(comment.getParentId(), 1);
	    }
		eventRepository.commentCountIncrement(id, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(comment);
	}

	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId, @RequestBody CommentEventModel updatedComment) {
		if (updatedComment.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}
		int updates = commentEventRepository.updateComment(commentId, creator, updatedComment.getContent());
		if (updates == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentEventModel> originalComment = commentEventRepository.findById(commentId);
		if (originalComment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		String newsId = originalComment.get().getEvent().getId();
		
		//Hiện tại children_comment_number của comment chỉ dùng để đếm comment con trực tiếp thôi, chứ không đếm comment cháu chắt
		//Còn nếu ông muốn đếm cả comment cháu chắt thì hú tui
		String parentId = originalComment.get().getParentId();
	    if (parentId != null) {
	        commentEventRepository.commentCountIncrement(parentId, -1);
	    }

		// Initilize variables
		List<CommentEventModel> childrenComments = new ArrayList<CommentEventModel>();
		List<String> allParentId = new ArrayList<String>();
		int totalDelete = 1;

		// Get children comments
		String curCommentId = commentId;
		allParentId.add(curCommentId);
		childrenComments.addAll(commentEventRepository.getChildrenComment(curCommentId));

		// Start the loop
		while (!childrenComments.isEmpty()) {
			CommentEventModel curComment = childrenComments.get(0);
			curCommentId = curComment.getId();
			List<CommentEventModel> temp = commentEventRepository.getChildrenComment(curCommentId);
			if (!temp.isEmpty()) {
				allParentId.add(curCommentId);
				childrenComments.addAll(temp);
			}

			childrenComments.remove(0);
		}

		// Delete all comments and update comment count
		commentEventRepository.deleteComment(commentId, creator);
		for (String iParentId : allParentId) {
			totalDelete += commentEventRepository.deleteChildrenComment(iParentId);
		}
		eventRepository.commentCountIncrement(newsId, -totalDelete);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
