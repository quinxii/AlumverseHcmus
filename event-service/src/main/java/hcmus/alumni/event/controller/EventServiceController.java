package hcmus.alumni.event.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Iterator;

import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

import hcmus.alumni.event.dto.CommentEventDto;
import hcmus.alumni.event.dto.ICommentEventDto;
import hcmus.alumni.event.dto.IEventDto;
import hcmus.alumni.event.dto.IParticipantEventDto;
import hcmus.alumni.event.model.EventModel;
import hcmus.alumni.event.model.StatusPostModel;
import hcmus.alumni.event.model.TagModel;
import hcmus.alumni.event.model.FacultyModel;
import hcmus.alumni.event.model.ParticipantEventId;
import hcmus.alumni.event.model.ParticipantEventModel;
import hcmus.alumni.event.model.UserModel;
import hcmus.alumni.event.model.CommentEventModel;
import hcmus.alumni.event.repository.CommentEventRepository;
import hcmus.alumni.event.repository.EventRepository;
import hcmus.alumni.event.repository.ParticipantEventRepository;
import hcmus.alumni.event.repository.TagRepository;
import hcmus.alumni.event.repository.UserRepository;
import hcmus.alumni.event.utils.ImageUtils;
import hcmus.alumni.event.utils.NotificationService;
import hcmus.alumni.event.utils.FirebaseService;
import hcmus.alumni.event.exception.AppException;
import jakarta.persistence.EntityManager;

import hcmus.alumni.event.common.CommentEventPermissions;
import hcmus.alumni.event.common.NotificationType;
import hcmus.alumni.event.model.notification.EntityTypeModel;
import hcmus.alumni.event.model.notification.NotificationChangeModel;
import hcmus.alumni.event.model.notification.NotificationModel;
import hcmus.alumni.event.model.notification.NotificationObjectModel;
import hcmus.alumni.event.model.notification.StatusNotificationModel;
import hcmus.alumni.event.repository.notification.EntityTypeRepository;
import hcmus.alumni.event.repository.notification.NotificationChangeRepository;
import hcmus.alumni.event.repository.notification.NotificationObjectRepository;
import hcmus.alumni.event.repository.notification.NotificationRepository;

@RestController
@RequestMapping("/events")
public class EventServiceController {
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private ParticipantEventRepository participantEventRepository;
	@Autowired
	private CommentEventRepository commentEventRepository;
	@Autowired
	private EntityTypeRepository entityTypeRepository;
	@Autowired
	private NotificationObjectRepository notificationObjectRepository;
	@Autowired
	private NotificationChangeRepository notificationChangeRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private ImageUtils imageUtils;
	@Autowired
	private FirebaseService firebaseService;
	@Autowired
	private NotificationService notificationService;
	
	private final static int MAXIMUM_PAGES = 50;
	private final static int MAXIMUM_TAGS = 5;
	
	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getEvents(
			@RequestHeader("userId") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "organizationTime") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "facultyId", required = false) Integer facultyId,
			@RequestParam(value = "tagNames", required = false) List<String> tagNames,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "mode", required = false, defaultValue = "1") int mode) {
	    if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
	    	pageSize = MAXIMUM_PAGES;
	    }
	    HashMap<String, Object> result = new HashMap<>();
	    if (tagNames != null) {
			for (int i = 0; i < tagNames.size(); i++) {
				tagNames.set(i, TagModel.sanitizeTagName(tagNames.get(i)));
			}
		}

	    try {
	        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
	        Page<IEventDto> events = null;
	        
	        Calendar cal = Calendar.getInstance();
	        Date startDate = cal.getTime();
	        events = eventRepository.searchEvents(userId, title, statusId, facultyId, tagNames, startDate, mode, pageable);

	        result.put("totalPages", events.getTotalPages());
	        result.put("events", events.getContent());
	    } catch (IllegalArgumentException e) {
	    	throw new AppException(50100, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(50101, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<IEventDto> getEventById(
			@PathVariable String id,
			@RequestHeader("userId") String userId) {
		
	    Optional<IEventDto> optionalEvent = eventRepository.findEventById(id, userId);
	    if (optionalEvent.isEmpty()) {
	    	throw new AppException(50200, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND);
	    }
	    eventRepository.incrementEventViews(id);
	    return ResponseEntity.status(HttpStatus.OK).body(optionalEvent.get());
	}
    
	@PreAuthorize("hasAnyAuthority('Event.Create')")
	@PostMapping("")
	public ResponseEntity<String> createEvent(
			@RequestHeader("userId") String creatorId,
	        @RequestParam(value = "title") String title, 
	        @RequestParam(value = "thumbnail") MultipartFile thumbnail,
	        @RequestParam(value = "content", required = false, defaultValue = "") String content,
	        @RequestParam(value = "organizationLocation", required = false, defaultValue = "") String organizationLocation,
	        @RequestParam(value = "organizationTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date organizationTime,
	        @RequestParam(value = "tagNames", required = false) List<String> tagNames,
	        @RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
	        @RequestParam(value = "minimumParticipants", required = false, defaultValue = "0") Integer minimumParticipants,
	        @RequestParam(value = "maximumParticipants", required = false, defaultValue = "0") Integer maximumParticipants,
	        @RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId) {
		if (title.equals("")) {
			throw new AppException(50300, "Title không được để trống", HttpStatus.BAD_REQUEST);
        }
		if (thumbnail.isEmpty()) {
			throw new AppException(50301, "thumbnail không được để trống", HttpStatus.BAD_REQUEST);
        }
		if (tagNames != null && tagNames.size() > MAXIMUM_TAGS) {
			throw new AppException(50303, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
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
	        if (tagNames != null && !tagNames.isEmpty()) {
				Set<TagModel> tags = new HashSet<TagModel>();
				for (String tagName : tagNames) {
					var sanitizedTagName = TagModel.sanitizeTagName(tagName);
					if (sanitizedTagName.isBlank()) {
						continue;
					}
					TagModel tag = tagRepository.findByName(sanitizedTagName);
					if (tag == null) {
						tag = new TagModel(sanitizedTagName);
					}
					tags.add(tag);
				}
				event.setTags(tags);
			}
	        if (!facultyId.equals(0)) {
	            event.setFaculty(new FacultyModel(facultyId));
	        }
	        event.setMinimumParticipants(minimumParticipants);
	        event.setMaximumParticipants(maximumParticipants);
	        event.setStatus(new StatusPostModel(statusId));
	        eventRepository.save(event);
	    } catch (IOException e) {
	    	e.printStackTrace();
			throw new AppException(50302, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	    return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('Event.Edit')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateEvent(
			@PathVariable String id,
			@RequestHeader("userId") String userId,
	        @RequestParam(value = "title", defaultValue = "") String title,
	        @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
	        @RequestParam(value = "content", required = false, defaultValue = "") String content,
	        @RequestParam(value = "organizationLocation", required = false, defaultValue = "") String organizationLocation,
	        @RequestParam(value = "organizationTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date organizationTime,
	        @RequestParam(value = "tagNames", required = false) List<String> tagNames,
	        @RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
	        @RequestParam(value = "minimumParticipants", required = false, defaultValue = "0") Integer minimumParticipants,
	        @RequestParam(value = "maximumParticipants", required = false, defaultValue = "0") Integer maximumParticipants,
	        @RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId) {
	    boolean isPut = false;
	    if (tagNames != null && tagNames.size() > MAXIMUM_TAGS) {
			throw new AppException(50401, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
	    
	    try {
	        // Find event
	        Optional<EventModel> optionalEvent = eventRepository.findById(id);
	        if (optionalEvent.isEmpty()) {
	        	throw new AppException(50400, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND);
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
	        if (tagNames != null) {
				Set<TagModel> currentTags = event.getTags();
				Set<TagModel> updatedTags = new HashSet<TagModel>();

				for (String tagName : tagNames) {
					var sanitizedTagName = TagModel.sanitizeTagName(tagName);
					if (sanitizedTagName.isBlank()) {
						continue;
					}
					updatedTags.add(new TagModel(sanitizedTagName));
				}
				// Remove tags
				for (Iterator<TagModel> iterator = currentTags.iterator(); iterator.hasNext();) {
					TagModel tag = iterator.next();
					if (!updatedTags.contains(tag)) {
						iterator.remove();
					}
				}
				// Add tags
				for (TagModel tag : updatedTags) {
					if (!currentTags.contains(tag)) {
						TagModel find = tagRepository.findByName(tag.getName());
						if (find == null) {
							find = new TagModel(tag.getName());
						}
						currentTags.add(find);
					}
				}
				event.setTags(currentTags);
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

	@PreAuthorize("hasAnyAuthority('Event.Delete')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteEvent(
			@PathVariable String id,
			@RequestHeader("userId") String userId) {
	    // Find event
	    Optional<EventModel> optionalEvent = eventRepository.findById(id);
	    if (optionalEvent.isEmpty()) {
	    	throw new AppException(50500, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND);
	    }
	    EventModel event = optionalEvent.get();
	    event.setStatus(new StatusPostModel(4));
	    eventRepository.save(event);

		List<String> commentIds = commentEventRepository.findByEventId(id);
		notificationService.deleteNotificationsByEntityIds(commentIds);

	    return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("/hot")
	public ResponseEntity<HashMap<String, Object>> getHotEvents(
			@RequestHeader("userId") String userId,
			@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
	    if (limit <= 0 || limit > 5) {
	        limit = 5;
	    }
	    Calendar cal = Calendar.getInstance();
	    Date startDate = cal.getTime();

	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "participants"));
	    Page<IEventDto> events = eventRepository.getHotEvents(userId, startDate, pageable);

	    HashMap<String, Object> result = new HashMap<>();
	    result.put("events", events.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/participated")
	public ResponseEntity<HashMap<String, Object>> getUserParticipatedEvents(
			@RequestHeader("userId") String requestingUserId,
			@RequestParam(value = "requestedUserId", required = true, defaultValue = "") String requestedUserId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "mode", required = false, defaultValue = "1") int mode) {
	    if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
	    	pageSize = MAXIMUM_PAGES;
	    }
	    HashMap<String, Object> result = new HashMap<>();

	    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "organizationTime"));
	    Calendar cal = Calendar.getInstance();
        Date startDate = cal.getTime();
        Page<IEventDto> events = eventRepository.getUserParticipatedEvents(requestingUserId, requestedUserId, startDate, mode, pageable);

        result.put("totalPages", events.getTotalPages());
        result.put("events", events.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/{id}/participants")
	public ResponseEntity<Map<String, Object>> getParticipantsListById(
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
	    if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
	    	pageSize = MAXIMUM_PAGES;
	    }
	    
	    Optional<EventModel> optionalEvent = eventRepository.findById(id);
	    if (optionalEvent.isEmpty()) {
	    	throw new AppException(50900, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND);
	    }
	    
	    Map<String, Object> result = new HashMap<>();
	    
	    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
	    Page<IParticipantEventDto> participantsPage = participantEventRepository.getParticipantsByEventId(id, pageable);

	    result.put("participants", participantsPage.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@PreAuthorize("hasAnyAuthority('Event.Participant.Create')")
	@PostMapping("/{id}/participants")
	public ResponseEntity<String> addParticipant(
	        @PathVariable String id,
	        @RequestHeader("userId") String userId,
	        @RequestBody ParticipantEventModel participantEvent) {
		Optional<IEventDto> optionalEvent = eventRepository.findEventById(id, userId);
		if (optionalEvent.isEmpty()) 
			throw new AppException(51000, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND);
		if (optionalEvent.get().getIsParticipated())
			throw new AppException(51001, "Đã tham gia sự kiện", HttpStatus.BAD_REQUEST);
		if (optionalEvent.get().getParticipants() >= optionalEvent.get().getMaximumParticipants()) 
			throw new AppException(51002, "Số lượng người tham gia sự kiện đã đạt mức tối đa", HttpStatus.BAD_REQUEST);
		
	    ParticipantEventModel participant = participantEvent;
	    participant.setId(new ParticipantEventId(id, userId));
	    participant.setCreatedAt(new Date());
	    participantEventRepository.save(participant);
	    
	    eventRepository.participantCountIncrement(id, 1);

	    return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Event.Participant.Delete')")
	@DeleteMapping("/{id}/participants")
	public ResponseEntity<String> deleteParticipant(
	        @PathVariable String id,
	        @RequestHeader("userId") String userId) {
	    // Delete the participant
	    int updatedRow = participantEventRepository.deleteByEventIdAndUserId(id, userId);

	    if (updatedRow == 1)
	    	eventRepository.participantCountIncrement(id, -1);
	    else
	    	throw new AppException(51100, "Không tìm thấy thành viên", HttpStatus.NOT_FOUND);

	    return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	@GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getEventComments(
			Authentication authentication,
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		boolean canDelete = false;
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("Event.Comment.Delete"))) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentEventDto> comments = commentEventRepository.getComments(id, userId, canDelete, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getEventChildrenComments(
			Authentication authentication,
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@PathVariable String commentId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentEventModel> parentComment = commentEventRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			throw new AppException(51300, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}
		
		boolean canDelete = false;
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("Event.Comment.Delete"))) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentEventDto> comments = commentEventRepository.getChildrenComment(commentId, userId, canDelete, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('Event.Comment.Create')")
	@PostMapping("/{id}/comments")
	@Transactional
	public ResponseEntity<HashMap<String, Object>> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentEventModel comment) {
		if (comment.getContent() == null || comment.getContent().equals("")) {
			throw new AppException(51400, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}

		HashMap<String, Object> result = new HashMap<String, Object>();
		
		comment.setId(UUID.randomUUID().toString());
		comment.setEvent(new EventModel(id));
		comment.setCreator(new UserModel(creator));
		
		try {
			CommentEventModel savedCmt = commentEventRepository.saveAndFlush(comment);
			entityManager.refresh(savedCmt);
			savedCmt.setPermissions(new CommentEventPermissions(true, true));
			result.put("comment", mapper.map(savedCmt, CommentEventDto.class));
		} catch (JpaObjectRetrievalFailureException e) {
			throw new AppException(51401, "Không tìm thấy sự kiện", HttpStatus.NOT_FOUND);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(51402, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}
		
		if (comment.getParentId() != null) {
			commentEventRepository.commentCountIncrement(comment.getParentId(), 1);
			// Fetch the parent comment
			CommentEventModel parentComment = commentEventRepository.findById(comment.getParentId())
					.orElseThrow(() -> new AppException(51402, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND));
			if (!parentComment.getCreator().getId().equals(creator)) {
				// Create NotificationObject
				EntityTypeModel entityType = entityTypeRepository
						.findByEntityTableAndNotificationType("comment_event", NotificationType.CREATE)
				        .orElseGet(() -> entityTypeRepository
							.save(new EntityTypeModel(null, "comment_event", NotificationType.CREATE, null)));
				NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType, 
						comment.getId(), new Date(), false);
				notificationObject = notificationObjectRepository.save(notificationObject);
				
				// Create NotificationChange
				NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject, 
						new UserModel(creator), false);
				notificationChangeRepository.save(notificationChange);
				
				// Create Notification
				NotificationModel notification = new NotificationModel(null, notificationObject, 
						parentComment.getCreator(), new StatusNotificationModel(1));
				notificationRepository.save(notification);
				
				Optional<UserModel> optionalUser = userRepository.findById(creator);
				firebaseService.sendNotification(
						notification, notificationChange, notificationObject,
						optionalUser.get().getAvatarUrl(),
						optionalUser.get().getFullName() + " đã bình luận về bình luận của bạn",
						comment.getEvent().getId());
			}
	    } else {
	    	eventRepository.commentCountIncrement(id, 1);
	    }
		
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@PreAuthorize("1 == @commentEventRepository.isCommentOwner(#commentId, #creator)")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId, @RequestBody CommentEventModel updatedComment) {
		if (updatedComment.getContent() == null || updatedComment.getContent().equals("")) {
			throw new AppException(51500, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}
		commentEventRepository.updateComment(commentId, creator, updatedComment.getContent());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Event.Comment.Delete') or 1 == @commentEventRepository.isCommentOwner(#commentId, #creator)")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentEventModel> optionalComment = commentEventRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			throw new AppException(51600, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}
		CommentEventModel originalComment = optionalComment.get();

		// Initilize variables
		List<CommentEventModel> childrenComments = new ArrayList<CommentEventModel>();
		List<String> allParentId = new ArrayList<String>();

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
		int deleted = commentEventRepository.deleteComment(commentId);
		for (String parentId : allParentId) {
			commentEventRepository.deleteChildrenComment(parentId);
		}
		if (deleted != 0) {
			if (originalComment.getParentId() != null) {
				commentEventRepository.commentCountIncrement(originalComment.getParentId(), -1);
			} else {
				eventRepository.commentCountIncrement(originalComment.getEvent().getId(), -1);
			}
		}
		
		// Delete notifications for the comment and its children
		List<String> allCommentIds = commentEventRepository.findByParentIds(allParentId);
		allCommentIds.add(commentId);
		notificationService.deleteNotificationsByEntityIds(allCommentIds);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	// Get a specific comment of a post
	@GetMapping("/{eventId}/comments/{commentId}")
	public ResponseEntity<Map<String, Object>> getSingleCommentOfAPost(
			Authentication authentication,
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@PathVariable String eventId,
			@PathVariable String commentId) {
		HashMap<String, Object> result = new HashMap<String, Object>();
	
		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("Event.Comment.Delete"))) {
			canDelete = true;
		}
	
		ICommentEventDto comment = commentEventRepository.getComment(eventId, commentId, userId, canDelete)
				.orElse(null);
		if (comment == null) {
			throw new AppException(51700, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}
	
		result.put("comment", comment);
	
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
