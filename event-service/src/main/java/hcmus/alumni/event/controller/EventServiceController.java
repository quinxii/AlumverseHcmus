package hcmus.alumni.event.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.event.model.EventModel;
import hcmus.alumni.event.repository.EventRepository;
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

//	@GetMapping("/news/count")
//	public ResponseEntity<Long> getPendingAlumniVerificationCount(
//			@RequestParam(value = "creator", required = false) String creator) {
//
//	}
	
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
	    Selection<Integer> statusIdSelection = root.get("statusId");
	    Selection<Integer> viewsSelection = root.get("views");
	    cq.multiselect(idSelection, creatorSelection, titleSelection, contentSelection, thumbnailSelection,
	            organizationLocationSelection, organizationTimeSelection, createAtSelection, updateAtSelection,
	            publishedAtSelection, statusIdSelection, viewsSelection);

	    // Where
	    Predicate statusPredicate;
	    switch (status) {
	        case "pending":
	            statusPredicate = cb.equal(root.get("statusId"), 1); // Assuming 1 represents pending status
	            break;
	        case "approved":
	            statusPredicate = cb.equal(root.get("statusId"), 2); // Assuming 2 represents approved status
	            break;
	        case "rejected":
	            statusPredicate = cb.equal(root.get("statusId"), 3); // Assuming 3 represents rejected status
	            break;
	        default:
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }

	    Predicate isDeletePredicate = cb.equal(root.get("isDelete"), false);
	    Predicate criteriaPredicate;
	    if (criteria.equals("title") || criteria.equals("content") || criteria.equals("organizationLocation")) {
	        criteriaPredicate = cb.like(root.get(criteria), "%" + keyword + "%");
	    } else {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	    }

	    cq.where(statusPredicate, isDeletePredicate, criteriaPredicate);

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
}
