package hcmus.alumni.group.controller;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
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

import hcmus.alumni.group.utils.ImageUtils;
import hcmus.alumni.group.dto.IGroupDto;
import hcmus.alumni.group.repository.GroupRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/groups")
public class GroupServiceController {
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private ImageUtils imageUtils;
	
	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getGroups(
		@RequestParam(value = "page", required = false, defaultValue = "0") int page,
		@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
		@RequestParam(value = "name", required = false, defaultValue = "") String name,
		@RequestParam(value = "orderBy", required = false, defaultValue = "createAt") String orderBy,
		@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
		@RequestParam(value = "statusId", required = false) Integer statusId) {
		
		if (pageSize == 0 || pageSize > 50) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		HashMap<String, Object> result = new HashMap<>();
		
		try {
		    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
		    Page<IGroupDto> groups = groupRepository.searchGroups(name, statusId, pageable);
		
		    result.put("totalPages", groups.getTotalPages());
		    result.put("groups", groups.getContent());
		} catch (IllegalArgumentException e) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<IGroupDto> getGroupById(@PathVariable String id) {
		Optional<IGroupDto> optionalGroup = groupRepository.findGroupById(id);
		if (optionalGroup.isEmpty()) {
		    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalGroup.get());
	}
	
	@PreAuthorize("hasAnyAuthority('Admin')")
	@PostMapping("")
	public ResponseEntity<String> createGroup(
	    @RequestHeader("userId") String creatorId,
	    @RequestParam(value = "name") String name
	    
	) {
		
		return null;
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateGroup(@PathVariable String id,
	    @RequestParam(value = "name", defaultValue = "") String name
	) {
		return null;
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteGroup(@PathVariable String id) {
		
		return null;
	}

}
