package hcmus.alumni.search.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.search.dto.ISearchDto;
import hcmus.alumni.search.repository.SearchRepository;
import hcmus.alumni.search.utils.ImageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/search")
public class SearchServiceController {
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private SearchRepository searchRepository;

	@GetMapping("/count")
	public ResponseEntity<Long> getSearchResultCount(@RequestParam(value = "status") String status) {
		if (status.equals("")) {
			ResponseEntity.status(HttpStatus.OK).body(0L);
		}
		return ResponseEntity.status(HttpStatus.OK).body(searchRepository.getCountByStatus(status));
	}

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getSearchResult(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "fullName", required = false, defaultValue = "") String fullName,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "facultyId", required = false) Integer facultyId) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<ISearchDto> searchResult = null;

			searchResult = searchRepository.searchUsers(fullName, statusId, facultyId, pageable);

			result.put("totalPages", searchResult.getTotalPages());
			result.put("searchResult", searchResult.getContent());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			result.put("error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}


}