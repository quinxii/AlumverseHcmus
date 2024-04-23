package hcmus.alumni.group.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.group.model.GroupModel;
import hcmus.alumni.group.model.GroupModel.Privacy;
import hcmus.alumni.group.model.UserModel;
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
            @RequestParam(value = "name") String name,
            @RequestParam(value = "type", required = false, defaultValue = "") String type,
            @RequestParam(value = "website", required = false, defaultValue = "") String website,
            @RequestParam(value = "privacy", required = false, defaultValue = "PUBLIC") Privacy privacy,
            @RequestParam(value = "avatar") MultipartFile avatar,
            @RequestParam(value = "cover") MultipartFile cover
	) {
		if (name.isEmpty() || avatar.isEmpty() || cover.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name, avatar and cover must not be empty");
        }
		if (avatar.getSize() > 5 * 1024 * 1024 || cover.getSize() > 5 * 1024 * 1024) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
	    }
        String id = UUID.randomUUID().toString();
        try {
            String avatarUrl = null;
            String coverUrl = null;

            // Save avatar image
            if (avatar != null) {
                avatarUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), avatar, "avatar");
            }

            // Save cover image
            if (cover != null) {
                coverUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), cover, "cover");
            }

            // Create group model
            GroupModel groupModel = new GroupModel();
            groupModel.setId(id);
            groupModel.setName(name);
            groupModel.setType(type);
            groupModel.setWebsite(website);
            groupModel.setPrivacy(privacy);
            groupModel.setCreator(new UserModel(creatorId)); // Assuming UserModel constructor takes userId
            groupModel.setAvatarUrl(avatarUrl);
            groupModel.setCoverUrl(coverUrl);

            groupRepository.save(groupModel);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save images");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateGroup(@PathVariable String id,
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "type", defaultValue = "") String type,
            @RequestParam(value = "website", defaultValue = "") String website,
            @RequestParam(value = "privacy", defaultValue = "") Privacy privacy,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "cover", required = false) MultipartFile cover
	) {
        try {
    		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
            if (optionalGroup.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
            }

            GroupModel groupModel = optionalGroup.get();
            boolean isPut = false;
            
            // Update name
            if (!name.isEmpty()) {
                groupModel.setName(name);
            }

            // Update type
            if (!type.isEmpty()) {
                groupModel.setType(type);
            }

            // Update website
            if (!website.isEmpty()) {
                groupModel.setWebsite(website);
            }

            // Update privacy
            if (privacy != null) {
                groupModel.setPrivacy(privacy);
            }

            // Update avatar
            if (avatar != null) {
                String avatarUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), avatar, "avatar");
                groupModel.setAvatarUrl(avatarUrl);
            }

            // Update cover
            if (cover != null) {
                String coverUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), cover, "cover");
                groupModel.setCoverUrl(coverUrl);
            }
            if (isPut)
            	groupRepository.save(groupModel);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save images");
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteGroup(@PathVariable String id) {
		
		return null;
	}

}
