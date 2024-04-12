package hcmus.alumni.event.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import hcmus.alumni.event.dto.IEventDto;
import hcmus.alumni.event.model.EventModel;
import hcmus.alumni.event.model.StatusPost;
import hcmus.alumni.event.model.UserModel;
import hcmus.alumni.event.repository.EventRepository;
import hcmus.alumni.event.repository.StatusPostRepository;
import hcmus.alumni.event.repository.UserRepository;
import hcmus.alumni.event.utils.ImageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/event")
public class EventServiceController {
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StatusPostRepository statusPostRepository;
	@Autowired
    private ImageUtils imageUtils;
	
	@GetMapping("/")
	public ResponseEntity<HashMap<String, Object>> getEvents(
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "title", required = false, defaultValue = "") String title,
	        @RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
	        @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
	        @RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId) {
	    if (pageSize == 0 || pageSize > 50) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	    HashMap<String, Object> result = new HashMap<>();

	    try {
	        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
	        Page<IEventDto> events = null;
	        if (statusId.equals(0)) {
	            events = eventRepository.searchEvents(title, pageable);
	        } else {
	            events = eventRepository.searchEventsByStatus(title, statusId, pageable);
	        }

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
    
	//@PreAuthorize("hasAnyAuthority('Admin')")
    @PostMapping("/")
    public ResponseEntity<EventModel> addEvent(@RequestHeader("userId") String creatorId,
                                               @RequestParam(required = false, defaultValue = "") String title,
                                               @RequestParam(required = false, defaultValue = "") String content,
                                               @RequestParam(required = false) MultipartFile thumbnail,
                                               @RequestParam(required = false, defaultValue = "") String organizationLocation,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date organizationTime,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publishedAt,
                                               @RequestParam(value = "tagsId[]", required = false, defaultValue = "") Integer[] tagsId,
                                   			   @RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
                                               @RequestParam(required = false, defaultValue = "0") String status) {
        try {
            UserModel creator = userRepository.findById(creatorId).orElse(null);

            Integer statusId;
            // Compare status string to retrieve the corresponding StatusPost entity ID
            switch (status) {
                case "Chờ":
                    statusId = 1;
                    break;
                case "Bình thường":
                    statusId = 2;
                    break;
                case "Ẩn":
                    statusId = 3;
                    break;
                case "Xoá":
                    statusId = 4;
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            Optional<StatusPost> statusPostOptional = statusPostRepository.findById(statusId);
            StatusPost statusPost = statusPostOptional.get();
            
            // Generate a new UUID as the event id
            String eventId = UUID.randomUUID().toString();

            // Create the EventModel object
            EventModel newEvent = new EventModel();
            newEvent.setId(eventId);
            newEvent.setCreator(creator);
            newEvent.setTitle(title);
            newEvent.setContent(content);
            
            // Save the thumbnail
            try {
                String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getEventPath(eventId), thumbnail);
                newEvent.setThumbnail(thumbnailUrl);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            newEvent.setOrganizationLocation(organizationLocation);
            newEvent.setOrganizationTime(organizationTime);
            newEvent.setPublishedAt(publishedAt);
            newEvent.setStatus(statusPost);
            newEvent.setViews(0);

            // Save the new event
            EventModel savedEvent = eventRepository.save(newEvent);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

	//@PreAuthorize("hasAnyAuthority('Admin')")
    @PutMapping("/{eventId}")
    public ResponseEntity<EventModel> updateEvent(@PathVariable String eventId,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam(required = false) String content,
                                                  @RequestParam(required = false) MultipartFile thumbnail,
                                                  @RequestParam(required = false) String organizationLocation,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date organizationTime,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publishedAt,
                                                  @RequestParam(value = "tagsId[]", required = false, defaultValue = "") Integer[] tagsId,
                                      			  @RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
                                                  @RequestParam(required = false) String status) {
        Optional<EventModel> eventOptional = eventRepository.findById(eventId);

        if (eventOptional.isPresent()) {
            EventModel existingEvent = eventOptional.get();

            // Update fields of existingEvent with non-null values from request parameters
            if (title != null)
                existingEvent.setTitle(title);
            if (content != null)
                existingEvent.setContent(content);
            // Update thumbnail if provided
            if (thumbnail != null) {
                try {
                    String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getEventPath(eventId), thumbnail);
                    existingEvent.setThumbnail(thumbnailUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
            if (organizationLocation != null)
                existingEvent.setOrganizationLocation(organizationLocation);
            if (organizationTime != null)
            	existingEvent.setOrganizationTime(organizationTime);
            if (publishedAt != null)
            	existingEvent.setPublishedAt(publishedAt);
            if (status != null) {
                Integer statusId;

                // Compare status string to retrieve the corresponding StatusPost entity ID
                switch (status) {
                    case "Chờ":
                        statusId = 1;
                        break;
                    case "Bình thường":
                        statusId = 2;
                        break;
                    case "Ẩn":
                        statusId = 3;
                        break;
                    case "Xoá":
                        statusId = 4;
                        break;
                    default:
                        return ResponseEntity.badRequest().build();
                }

                Optional<StatusPost> statusPostOptional = statusPostRepository.findById(statusId);
                existingEvent.setStatus(statusPostOptional.get());
            }

            try {
                EventModel savedEvent = eventRepository.save(existingEvent);
                return ResponseEntity.ok(savedEvent);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
