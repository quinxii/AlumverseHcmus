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
	public ResponseEntity<HashMap<String, Object>> searchEvent(
	        @RequestParam String status,
	        @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
	        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
	        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
	        @RequestParam(value = "createAtOrder", required = false, defaultValue = "desc") String createAtOrder,
	        @RequestParam(value = "organizationTimeOrder", required = false, defaultValue = "") String organizationTimeOrder,
	        @RequestParam(value = "viewsOrder", required = false, defaultValue = "") String viewsOrder) {

	    // Initialize CriteriaBuilder
	    CriteriaBuilder cb = em.getCriteriaBuilder();

	    // Create CriteriaQuery for the DTO class
	    CriteriaQuery<EventModel> cq = cb.createQuery(EventModel.class);

	    // Create Root entity
	    Root<EventModel> root = cq.from(EventModel.class);

	    // Select
	    Selection<String> idSelection = root.get("id");
	    Selection<String> creatorSelection = root.get("creator");
	    Selection<String> titleSelection = root.get("title");
	    Selection<String> contentSelection = root.get("content");
	    Selection<String> thumbnailSelection = root.get("thumbnail");
	    Selection<String> organizationLocationSelection = root.get("organizationLocation");
	    Selection<Date> organizationTimeSelection = root.get("organizationTime");
	    Selection<Date> createAtSelection = root.get("createAt");
	    Selection<Date> updateAtSelection = root.get("updateAt");
	    Selection<Date> publishedAtSelection = root.get("publishedAt");
	    Selection<StatusPost> statusIdSelection = root.get("statusId");
	    Selection<Integer> viewsSelection = root.get("views");
	    cq.multiselect(idSelection, creatorSelection, titleSelection, contentSelection, thumbnailSelection,
	            organizationLocationSelection, organizationTimeSelection, createAtSelection, updateAtSelection,
	            publishedAtSelection, statusIdSelection, viewsSelection);

	    // Where
	    Predicate statusPredicate;
	    switch (status) {
	        case "Chờ":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 1); // Assuming 1 represents pending status
	            break;
	        case "Bình thường":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 2); 
	            break;
	        case "Ẩn":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 3); 
	            break;
	        case "Xoá":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 4); 
	            break;
	        default:
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }

	    Predicate criteriaPredicate;
	    criteriaPredicate = cb.like(root.get("title"), "%" + keyword + "%");

	    cq.where(statusPredicate, criteriaPredicate);

	    // Order by
	    List<Order> orderList = new ArrayList<>();
	    if (createAtOrder.equals("asc")) {
	        orderList.add(cb.asc(root.get("createAt")));
	    } else if (createAtOrder.equals("desc")) {
	        orderList.add(cb.desc(root.get("createAt")));
	    }
	    if (organizationTimeOrder.equals("asc")) {
	        orderList.add(cb.asc(root.get("organizationTime")));
	    } else if (organizationTimeOrder.equals("desc")) {
	        orderList.add(cb.desc(root.get("organizationTime")));
	    }
	    if (viewsOrder.equals("asc")) {
	        orderList.add(cb.asc(root.get("views")));
	    } else if (viewsOrder.equals("desc")) {
	        orderList.add(cb.desc(root.get("views")));
	    }
	    cq.orderBy(orderList);

	    // Create HashMap for result
	    HashMap<String, Object> result = new HashMap<>();
	    TypedQuery<EventModel> typedQuery = em.createQuery(cq);
	    typedQuery.setFirstResult(offset);
	    typedQuery.setMaxResults(limit);
	    result.put("itemNumber", typedQuery.getResultList().size());
	    result.put("items", typedQuery.getResultList());

	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}

    // Endpoint to get a specific event by ID
    @GetMapping("/{eventId}")
    public ResponseEntity<EventModel> getEventById(@PathVariable String eventId) {
        Optional<EventModel> eventOptional = eventRepository.findById(eventId);
        
        return eventOptional.map(ResponseEntity::ok)
                            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/")
    public ResponseEntity<EventModel> addEvent(@RequestHeader("userId") String creatorId,
                                               @RequestParam String title,
                                               @RequestParam String content,
                                               @RequestParam MultipartFile thumbnail,
                                               @RequestParam String organizationLocation,
                                               @RequestParam Date organizationTime,
                                               @RequestParam Date publishedAt,
                                               @RequestParam String status) {
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
            
            // Generate a new UUID as the image name for the thumbnail URL
            String imageName = UUID.randomUUID().toString();

            // Save the thumbnail
            try {
                String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getEventPath(imageName), thumbnail);
                newEvent.setThumbnail(thumbnailUrl);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            newEvent.setOrganizationLocation(organizationLocation);
            newEvent.setOrganizationTime(organizationTime);
            newEvent.setPublishedAt(publishedAt);
            newEvent.setStatusId(statusPost);
            newEvent.setViews(0);

            // Save the new event
            EventModel savedEvent = eventRepository.save(newEvent);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventModel> updateEvent(@PathVariable String eventId,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam(required = false) String content,
                                                  @RequestParam(required = false) MultipartFile thumbnail,
                                                  @RequestParam(required = false) String organizationLocation,
                                                  @RequestParam(required = false) Date organizationTime,
                                                  @RequestParam(required = false) Date publishedAt,
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
                existingEvent.setStatusId(statusPostOptional.get());
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
