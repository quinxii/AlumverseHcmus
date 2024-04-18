package hcmus.alumni.event.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.event.dto.IEventDto;
import hcmus.alumni.event.dto.IParticipantEventDto;
import hcmus.alumni.event.model.EventModel;
import hcmus.alumni.event.model.StatusPostModel;
import hcmus.alumni.event.model.FacultyModel;
import hcmus.alumni.event.model.UserModel;
import hcmus.alumni.event.repository.EventRepository;
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
    private ImageUtils imageUtils;
	
	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getEvents(
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "title", required = false, defaultValue = "") String title,
	        @RequestParam(value = "orderBy", required = false, defaultValue = "organizationTime") String orderBy,
	        @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
	        @RequestParam(value = "facultyId", required = false) Integer facultyId,
	        @RequestParam(value = "statusId", required = false) Integer statusId) {
	    if (pageSize == 0 || pageSize > 50) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }
	    HashMap<String, Object> result = new HashMap<>();

	    try {
	        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
	        Page<IEventDto> events = null;
	        
	        events = eventRepository.searchEvents(title, statusId, facultyId, pageable);

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
	
	@GetMapping("/most-participants")
	public ResponseEntity<HashMap<String, Object>> getMostViewedEvents(
	        @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
	    if (limit <= 0 || limit > 5) {
	        limit = 5;
	    }
	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "participants"));
	    Page<IEventDto> events = eventRepository.getMostParticipantsEvents(pageable);

	    HashMap<String, Object> result = new HashMap<>();
	    result.put("events", events.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/hot")
	public ResponseEntity<HashMap<String, Object>> getHotEvents(
	        @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
	    if (limit <= 0 || limit > 5) {
	        limit = 5;
	    }
	    Calendar cal = Calendar.getInstance();
	    Date endDate = cal.getTime();
	    cal.add(Calendar.WEEK_OF_YEAR, -1);
	    Date startDate = cal.getTime();

	    Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "participants"));
	    Page<IEventDto> events = eventRepository.getHotEvents(startDate, endDate, pageable);

	    HashMap<String, Object> result = new HashMap<>();
	    result.put("events", events.getContent());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/{id}/participant")
	public ResponseEntity<Map<String, Object>> getParticipantsListById(@PathVariable String id) {
	    Map<String, Object> result = new HashMap<>();
	   
	    List<IParticipantEventDto> participantList = eventRepository.getParticipantsByEventId(id);
	    Integer participantCount = participantList.size();
	    		
	    result.put("participantCount", participantCount);
	    result.put("participants", participantList);
	    
	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
