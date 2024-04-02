package hcmus.alumni.event.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.event.model.EventModel;
import hcmus.alumni.event.model.StatusPost;
import hcmus.alumni.event.model.UserModel;
import hcmus.alumni.event.repository.EventRepository;
import hcmus.alumni.event.repository.StatusPostRepository;
import hcmus.alumni.event.repository.UserRepository;
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
	
	@GetMapping("/")
	public ResponseEntity<HashMap<String, Object>> searchEvent(
	        @RequestParam String status,
	        @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
	        @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
	        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
	        @RequestParam(value = "criteria", required = false, defaultValue = "title") String criteria,
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
	        case "pending":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 1); // Assuming 1 represents pending status
	            break;
	        case "normal":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 2); 
	            break;
	        case "hide":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 3); 
	            break;
	        case "deleted":
	            statusPredicate = cb.equal(root.get("statusId").get("id"), 4); 
	            break;
	        default:
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }

	    Predicate criteriaPredicate;
	    if (criteria.equals("title") || criteria.equals("content") || criteria.equals("organizationLocation")) {
	        criteriaPredicate = cb.like(root.get(criteria), "%" + keyword + "%");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }

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
	    result.put("itemNumber", typedQuery.getResultList().size());
	    typedQuery.setFirstResult(offset);
	    typedQuery.setMaxResults(limit);
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
    public ResponseEntity<EventModel> addEvent(@RequestBody Map<String, Object> eventModel) {
    	System.out.println(1111);
        try {
            // Extract data from the map
            String creatorId = (String) eventModel.get("creator");
            UserModel creator = userRepository.findById(creatorId).orElse(null);
            if (creator == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
          
            String title = (String) eventModel.get("title");
            String content = (String) eventModel.get("content");
            String thumbnail = (String) eventModel.get("thumbnail");
            String organizationLocation = (String) eventModel.get("organizationLocation");
            Date organizationTime = parseDateString((String) eventModel.get("organizationTime"));
            Date publishedAt = parseDateString((String) eventModel.get("publishedAt"));
            Integer statusId = (Integer) eventModel.get("statusId");
            Optional<StatusPost> statusPostOptional = statusPostRepository.findById(statusId);

            // Check if StatusPost with the given ID exists
            if (statusPostOptional.isPresent()) {
                StatusPost statusPost = statusPostOptional.get();
                // Create the EventModel object
                EventModel newEvent = new EventModel();
                newEvent.setCreator(creator);
                newEvent.setTitle(title);
                newEvent.setContent(content);
                newEvent.setThumbnail(thumbnail);
                newEvent.setOrganizationLocation(organizationLocation);
                newEvent.setOrganizationTime(organizationTime);
                newEvent.setPublishedAt(publishedAt);
                newEvent.setStatusId(statusPost);
                newEvent.setViews(0);

                // Save the new event
                EventModel savedEvent = eventRepository.save(newEvent);

                return ResponseEntity.status(HttpStatus.CREATED).body(savedEvent);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
        	e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @PutMapping("/{eventId}")
    public ResponseEntity<EventModel> updateEvent(@PathVariable String eventId, 
            @RequestBody Map<String, Object> updatedEvent) {
        Optional<EventModel> eventOptional = eventRepository.findById(eventId);
        
        if (eventOptional.isPresent()) {
            EventModel existingEvent = eventOptional.get();
            
            // Update fields of existingEvent with non-null values from updatedEvent
            if (updatedEvent.containsKey("title"))
                existingEvent.setTitle((String) updatedEvent.get("title"));
            if (updatedEvent.containsKey("content"))
                existingEvent.setContent((String) updatedEvent.get("content"));
            if (updatedEvent.containsKey("thumbnail"))
                existingEvent.setThumbnail((String) updatedEvent.get("thumbnail"));
            if (updatedEvent.containsKey("organizationLocation"))
                existingEvent.setOrganizationLocation((String) updatedEvent.get("organizationLocation"));
            if (updatedEvent.containsKey("organizationTime"))
				try {
					existingEvent.setOrganizationTime(parseDateString((String) updatedEvent.get("organizationTime")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
            if (updatedEvent.containsKey("publishedAt"))
				try {
					existingEvent.setPublishedAt(parseDateString((String) updatedEvent.get("publishedAt")));
				} catch (ParseException e) {
					e.printStackTrace();
				}
            if (updatedEvent.containsKey("statusId")) {
                Integer statusId = (Integer) updatedEvent.get("statusId");
                Optional<StatusPost> statusPostOptional = statusPostRepository.findById(statusId);
                if (statusPostOptional.isPresent()) {
                    existingEvent.setStatusId(statusPostOptional.get());
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
            if (updatedEvent.containsKey("views"))
                existingEvent.setViews((Integer) updatedEvent.get("views"));

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

    
    private Date parseDateString(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dateFormat.parse(dateString);
    }
}
