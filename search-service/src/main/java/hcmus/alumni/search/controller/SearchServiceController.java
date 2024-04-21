package hcmus.alumni.search.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.search.model.UserModel;
import hcmus.alumni.search.repository.SearchRepository;
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

	@GetMapping("")
	public ResponseEntity<List<UserModel>> getSearchResult(@RequestParam(value = "find", defaultValue = "") String id) {
		// Assuming full name is relevant for user search
		List<UserModel> similarUsers = searchRepository.searchUsers(id);
		// You can process similarUsers here if needed
		return ResponseEntity.status(HttpStatus.OK).body(similarUsers);
	}

}