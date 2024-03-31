package hcmus.alumni.halloffame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hcmus.alumni.halloffame.repository.HallOfFameRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/news")
public class HallOfFameServiceController {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private HallOfFameRepository halloffameRepository;

//	@GetMapping("/news/count")
//	public ResponseEntity<Long> getPendingAlumniVerificationCount(
//			@RequestParam(value = "creator", required = false) String creator) {
//
//	}
	
	
}
