package hcmus.alumni.halloffame.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.halloffame.repository.HallOfFameRepository;
import hcmus.alumni.halloffame.utils.ImageUtils;
import hcmus.alumni.halloffame.dto.HallOfFameDto;
import hcmus.alumni.halloffame.model.HallOfFameModel;
import hcmus.alumni.halloffame.model.StatusPostModel;
import hcmus.alumni.halloffame.model.UserModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/hall-of-fame")
public class HallOfFameServiceController {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private HallOfFameRepository halloffameRepository;
	@Autowired
	private ImageUtils imageUtils;

	@GetMapping("/count")
	public ResponseEntity<Long> getPendingAlumniVerificationCount(@RequestParam(value = "status") String status) {
		if (status.equals("")) {
			ResponseEntity.status(HttpStatus.OK).body(0);
		}
		return ResponseEntity.status(HttpStatus.OK).body(halloffameRepository.getCountByStatus(status));
	}

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getHallOfFame(
			@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
			@RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
			@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
			@RequestParam(value = "createAtOrder", required = false, defaultValue = "desc") String createAtOrder) {
		// Initiate
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<HallOfFameDto> cq = cb.createQuery(HallOfFameDto.class);
		// From
		Root<HallOfFameModel> root = cq.from(HallOfFameModel.class);

		// Join
//		Join<NewsModel, UserModel> userJoin = root.join("user", JoinType.INNER);
		Join<HallOfFameModel, StatusPostModel> statusJoin = root.join("status", JoinType.INNER);
		
		// Select
		Selection<String> idSelection = root.get("id");
		Selection<String> titleSelection = root.get("title");
		Selection<String> contentSelection = root.get("content");
		Selection<String> thumbnailSelection = root.get("thumbnail");
		Selection<Integer> viewsSelection = root.get("views");
		Selection<String> facultySelection = root.get("faculty");
		Selection<Integer> beginningYearSelection = root.get("beginningYear");
		cq.multiselect(idSelection, titleSelection, contentSelection, thumbnailSelection, viewsSelection, facultySelection, beginningYearSelection);
		
		// Where
		Predicate titlePredicate = cb.like(root.get("title"), "%" + keyword + "%");
		Predicate statusPredicate = cb.equal(statusJoin.get("name"), "Bình thường");
		cq.where(titlePredicate, statusPredicate);
		
		// Order by
		List<Order> orderList = new ArrayList<Order>();
		if (createAtOrder.equals("asc")) {
			orderList.add(cb.asc(root.get("createAt")));
		} else if (createAtOrder.equals("desc")) {
			orderList.add(cb.desc(root.get("createAt")));
		}
		cq.orderBy(orderList);
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		TypedQuery<HallOfFameDto> typedQuery = em.createQuery(cq);
		result.put("itemNumber", typedQuery.getResultList().size());
		typedQuery.setFirstResult(offset);
		typedQuery.setMaxResults(limit);
		result.put("items", typedQuery.getResultList());
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PostMapping("")
	public ResponseEntity<String> createHallOfFame(@RequestHeader("userId") String creator,
			@RequestParam(value = "title") String title, @RequestParam(value = "content") String content,
			@RequestParam(value = "thumbnail") MultipartFile thumbnail,
	        @RequestParam(value = "faculty") String faculty,
		    @RequestParam(value = "beginningYear") Integer beginningYear){
		if (creator.isEmpty() || title.isEmpty() || content.isEmpty() || thumbnail.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields must not be empty");
		}
		if (thumbnail.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
		}
		String id = UUID.randomUUID().toString();
		
	    // Parse the HTML content
	    Document doc = Jsoup.parse(content);
	    // Select all img elements with the src attribute
	    Elements imgTags = doc.select("img[src]");
	    // Loop through each img tag and save each to storage
	    try {
	    	Integer contentImgIdx = 0;
		    for (Element img : imgTags) {
			      String src = img.attr("src");
			      System.out.println(src);
			      String newSrc = imageUtils.saveBase64ImageToStorage(imageUtils.getNewsPath(id), src, contentImgIdx.toString());
			      img.attr("src", newSrc);
			      contentImgIdx++;
			    }
		} catch (IOException e) {
			// TODO: handle exception
			System.err.println(e);
		}
	    doc.outputSettings().indentAmount(0).prettyPrint(false);
	    String processedContent = doc.body().html();

		try {
			// Save thumbnail image
			String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getHallOfFamePath(id), thumbnail, "thumbnail");
			// Save news to database
			HallOfFameModel halloffame = new HallOfFameModel(id, creator, title, processedContent, thumbnailUrl, faculty, beginningYear);
			halloffameRepository.save(halloffame);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body("");
	}

	@PutMapping("/{id}")
	public ResponseEntity<String> updateHallOfFame(@PathVariable String id,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "content", required = false) String content,
			@RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
			@RequestParam(value = "faculty") String faculty,
		    @RequestParam(value = "beginningYear") Integer beginningYear) {
		if (thumbnail != null && thumbnail.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
		}

		try {
			// Find news
			Optional<HallOfFameModel> optionalHallOfFame = halloffameRepository.findById(id);
			if (optionalHallOfFame.empty() != null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
			}
			HallOfFameModel halloffame = optionalHallOfFame.get();
			if (title != null) {
				halloffame.setTitle(title);
			}
			if (content != null) {
				halloffame.setContent(content);
			}
			if (thumbnail != null) {
				// Overwrite old thumbnail
				imageUtils.saveImageToStorage(imageUtils.getHallOfFamePath(id), thumbnail, "thumbnail");
			}
			if (faculty != null) {
				halloffame.setFaculty(faculty);
			}
			if (beginningYear != null) {
				halloffame.setBeginningYear(beginningYear);
			}
			if (title != null | content != null | faculty != null | beginningYear != null)
				halloffameRepository.save(halloffame);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
}