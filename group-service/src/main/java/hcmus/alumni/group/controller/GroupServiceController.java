package hcmus.alumni.group.controller;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.hibernate.query.sqm.UnknownPathException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import hcmus.alumni.group.common.Privacy;
import hcmus.alumni.group.common.GroupMemberRole;
import hcmus.alumni.group.model.GroupModel;
import hcmus.alumni.group.model.UserModel;
import hcmus.alumni.group.model.StatusPostModel;
import hcmus.alumni.group.model.StatusUserGroupModel;
import hcmus.alumni.group.model.GroupUserId;
import hcmus.alumni.group.model.GroupMemberModel;
import hcmus.alumni.group.model.RequestJoinGroupModel;
import hcmus.alumni.group.model.PostGroupModel;
import hcmus.alumni.group.model.StatusPostModel;
import hcmus.alumni.group.model.CommentPostGroupModel;
import hcmus.alumni.group.model.PicturePostGroupModel;
import hcmus.alumni.group.model.InteractPostGroupId;
import hcmus.alumni.group.model.InteractPostGroupModel;
import hcmus.alumni.group.model.ReactModel;
import hcmus.alumni.group.utils.ImageUtils;
import hcmus.alumni.group.dto.IGroupDto;
import hcmus.alumni.group.dto.IGroupMemberDto;
import hcmus.alumni.group.dto.IRequestJoinGroupDto;
import hcmus.alumni.group.dto.IPostGroupDto;
import hcmus.alumni.group.dto.ICommentPostGroupDto;
import hcmus.alumni.group.dto.IInteractPostGroupDto;
import hcmus.alumni.group.dto.ReactRequestDto;
import hcmus.alumni.group.dto.IUserDto;
import hcmus.alumni.group.repository.GroupRepository;
import hcmus.alumni.group.repository.GroupMemberRepository;
import hcmus.alumni.group.repository.RequestJoinGroupRepository;
import hcmus.alumni.group.repository.PostGroupRepository;
import hcmus.alumni.group.repository.CommentPostGroupRepository;
import hcmus.alumni.group.repository.InteractPostGroupRepository;
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
	private GroupMemberRepository groupMemberRepository;
	@Autowired
	private RequestJoinGroupRepository requestJoinGroupRepository;
	@Autowired
	private PostGroupRepository postGroupRepository;
	@Autowired
	private CommentPostGroupRepository commentPostGroupRepository;
	@Autowired
	private InteractPostGroupRepository interactPostGroupRepository;
	@Autowired
	private ImageUtils imageUtils;
	
	private final int MAX_IMAGE_SIZE_PER_POST = 5;
	
	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getGroups(
			Authentication authentication,
			@RequestHeader("userId") String requestingUserId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "name", required = false, defaultValue = "") String name,
			@RequestParam(value = "orderBy", required = false, defaultValue = "createAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId,
			@RequestParam(value = "privacy", required = false) Privacy privacy,
			@RequestParam(value = "isJoined", required = false) Boolean isJoined) {

		if (pageSize == 0 || pageSize > 50) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		HashMap<String, Object> result = new HashMap<>();
		
		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Group.Delete"))) {
			canDelete = true;
		}
		
		try {
		    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
		    Page<IGroupDto> groups = groupRepository.searchGroups(name, statusId, privacy, isJoined, requestingUserId, canDelete, pageable);
		
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
	public ResponseEntity<IGroupDto> getGroupById(
			Authentication authentication,
			@PathVariable String id, 
			@RequestHeader("userId") String requestingUserId) {
		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Group.Delete"))) {
			canDelete = true;
		}
		
	    Optional<IGroupDto> optionalGroup = groupRepository.findGroupById(id, requestingUserId, canDelete);
	    if (optionalGroup.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    }

	    return ResponseEntity.status(HttpStatus.OK).body(optionalGroup.get());
	}
	
	@GetMapping("/{id}/joined-friends")
	public ResponseEntity<Set<IUserDto>> getJoinedFriendsByGroupId(@PathVariable String id, @RequestHeader("userId") String requestingUserId) {
		Set<IUserDto> joinedFriends = groupMemberRepository.findJoinedFriends(id, requestingUserId);

	    return ResponseEntity.status(HttpStatus.OK).body(joinedFriends);
	}
	
	@PreAuthorize("hasAnyAuthority('Group.Create')")
	@PostMapping("")
	public ResponseEntity<String> createGroup(
			@RequestHeader("userId") String creatorId,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "type", required = false, defaultValue = "") String type,
            @RequestParam(value = "website", required = false, defaultValue = "") String website,
            @RequestParam(value = "privacy", required = false, defaultValue = "PUBLIC") Privacy privacy,
            @RequestParam(value = "cover") MultipartFile cover,
            @RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId
	) {
		if (name.isEmpty() || cover.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Name and cover must not be empty");
        }
		if (cover.getSize() > 5 * 1024 * 1024) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
	    }
        String id = UUID.randomUUID().toString();
        try {
            String coverUrl = null;

            // Save cover image
            if (cover != null) {
                coverUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), cover, "cover");
            }

            // Create group model
            GroupModel groupModel = new GroupModel();
            groupModel.setId(id);
            groupModel.setName(name);
            groupModel.setDescription(description);
            groupModel.setType(type);
            groupModel.setWebsite(website);
            groupModel.setPrivacy(privacy);
            groupModel.setCreator(new UserModel(creatorId));
            groupModel.setCoverUrl(coverUrl);
            groupModel.setStatus(new StatusUserGroupModel(statusId));
            groupModel.setParticipantCount(1);
            
            groupRepository.save(groupModel);
            
			// Add creator as ADMIN
			GroupMemberModel member = new GroupMemberModel();
			GroupUserId groupUserId = new GroupUserId();
			groupUserId.setGroup(new GroupModel(id));
			groupUserId.setUser(new UserModel(creatorId));
			member.setId(groupUserId);
			member.setCreateAt(new Date());
			member.setRole(GroupMemberRole.CREATOR);
			groupMemberRepository.save(member);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save images");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"CREATOR\") or "
			+ "1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"ADMIN\")")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateGroup(@PathVariable String id,
			@RequestHeader("userId") String requestingUserId,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "description", required = false, defaultValue = "") String description,
            @RequestParam(value = "type", required = false, defaultValue = "") String type,
            @RequestParam(value = "website", required = false, defaultValue = "") String website,
            @RequestParam(value = "privacy", required = false, defaultValue = "") Privacy privacy,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "statusId", required = false) Integer statusId
	) {
        try {
    		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
            GroupModel groupModel = optionalGroup.get();
            boolean isPut = false;
            
            if (!name.isEmpty()) {
                groupModel.setName(name);
                isPut = true;
            }
            
            if (!description.isEmpty()) {
                groupModel.setDescription(description);
                isPut = true;
            }

            if (!type.isEmpty()) {
                groupModel.setType(type);
                isPut = true;
            }

            if (!website.isEmpty()) {
                groupModel.setWebsite(website);
                isPut = true;
            }

            if (privacy != null) {
                groupModel.setPrivacy(privacy);
                isPut = true;
            }

            if (cover != null) {
                String coverUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), cover, "cover");
                groupModel.setCoverUrl(coverUrl);
                isPut = true;
            }
            
            if (statusId != null) {
            	groupModel.setStatus(new StatusUserGroupModel(statusId));
                isPut = true;
            }
            if (isPut)
            	groupRepository.save(groupModel);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save images");
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('Group.Delete') or "
			+ "1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"CREATOR\")")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteGroup(
			@PathVariable String id,
			@RequestHeader("userId") String requestingUserId) {
		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
	    GroupModel group = optionalGroup.get();
	    group.setStatus(new StatusUserGroupModel(3));
	    groupRepository.save(group);
	    
	    groupMemberRepository.deleteAllGroupMember(id);
	    
	    return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	@GetMapping("/{id}/members")
	public ResponseEntity<HashMap<String, Object>> getGroupMembersByGroupId(
			@PathVariable String id,
		    @RequestParam(value = "page", defaultValue = "0") int page,
		    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
		    @RequestParam(value = "role", required = false) GroupMemberRole role
	) {
		
		if (pageSize == 0 || pageSize > 50) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		HashMap<String, Object> result = new HashMap<>();
		
		Pageable pageable = PageRequest.of(page, pageSize);
	    Page<IGroupMemberDto> members = groupMemberRepository.searchMembers(id, role, pageable);
	
	    result.put("totalPages", members.getTotalPages());
	    result.put("members", members.getContent());
	    
	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@PreAuthorize("1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"CREATOR\") or "
			+ "1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"ADMIN\")")
	@PutMapping("/{id}/members/{userId}")
    public ResponseEntity<String> updateGroupMemberRole(
    		@PathVariable String id, 
    		@PathVariable String userId,
    		@RequestHeader("userId") String requestingUserId,
    		@RequestBody GroupMemberModel updatedGroupMember) {
		Optional<GroupMemberModel> optionalMember = groupMemberRepository.findByGroupIdAndUserId(id, requestingUserId);
		GroupMemberModel member = optionalMember.get();
		if (requestingUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You can not change your role");
		}
		if (updatedGroupMember.getRole() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
		}
		int updates = groupMemberRepository.updateGroupMember(id, userId, updatedGroupMember.getRole());
		if (updates == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		return ResponseEntity.status(HttpStatus.OK).body(null);
    }

	@PreAuthorize("1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"CREATOR\") or "
			+ "1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"ADMIN\") or "
			+ "#userId == #requestingUserId")
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<String> deleteGroupMember(
    		@PathVariable String id, 
    		@PathVariable String userId,
    		@RequestHeader("userId") String requestingUserId) {
    	Optional<GroupMemberModel> optionalMember = groupMemberRepository.findByGroupIdAndUserId(id, requestingUserId);
    	GroupMemberModel member = optionalMember.get();
        
    	int delete = groupMemberRepository.deleteGroupMember(id, userId);
    	
    	if (delete != 0) {
    		groupRepository.participantCountIncrement(id, -1);
    		return ResponseEntity.status(HttpStatus.OK).body(null);
    	}
    	else
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    
	@PreAuthorize("1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"CREATOR\") or "
			+ "1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"ADMIN\")")
    @GetMapping("/{id}/requests")
    public ResponseEntity<HashMap<String, Object>> getRequestJoinsByGroupId(
        @PathVariable String id,
        @RequestHeader("userId") String requestingUserId,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        if (pageSize == 0 || pageSize > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        HashMap<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<IRequestJoinGroupDto> requests = requestJoinGroupRepository.searchRequestJoin(id, pageable);

        result.put("totalPages", requests.getTotalPages());
        result.put("requests", requests.getContent());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    
    @PostMapping("/{id}/requests")
    public ResponseEntity<String> addRequestJoin(
    		@PathVariable String id,
    		@RequestHeader("userId") String userId) {
    	Optional<GroupModel> optionalGroup = groupRepository.findById(id);
	    if (optionalGroup.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
	    }
    	
    	// Check if the user is already a member of the group
        Optional<GroupMemberModel> existingMemberOptional = groupMemberRepository.findByGroupIdAndUserId(id, userId);
        if (existingMemberOptional.isPresent() && !existingMemberOptional.get().isDelete()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You're already in this group.");
        }

        // Check if there's a pending request to join the group
        Optional<RequestJoinGroupModel> pendingRequestOptional = requestJoinGroupRepository.findByGroupIdAndUserId(id, userId);
        if (pendingRequestOptional.isPresent() && !pendingRequestOptional.get().isDelete()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Your request to join this group is pending.");
        }
        
        //create request if group is private, auto join as member if group is public
        GroupModel group = optionalGroup.get();
	    if (group.getPrivacy().equals(Privacy.PRIVATE)) {
	    	RequestJoinGroupModel newRequestJoin = new RequestJoinGroupModel();
			GroupUserId groupUserId = new GroupUserId();
			groupUserId.setGroup(new GroupModel(id));
			groupUserId.setUser(new UserModel(userId));
			newRequestJoin.setId(groupUserId);
			newRequestJoin.setCreateAt(new Date());
			requestJoinGroupRepository.save(newRequestJoin);
        } else {
			GroupMemberModel member = new GroupMemberModel();
			GroupUserId groupUserId = new GroupUserId();
			groupUserId.setGroup(new GroupModel(id));
			groupUserId.setUser(new UserModel(userId));
			member.setId(groupUserId);
			member.setCreateAt(new Date());
			member.setRole(GroupMemberRole.MEMBER);
			groupMemberRepository.save(member);
			groupRepository.participantCountIncrement(id, 1);
        }
		
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

	@PreAuthorize("1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"CREATOR\") or "
			+ "1 == @groupMemberRepository.hasGroupMemberRole(#id, #requestingUserId, \"ADMIN\")")
    @PutMapping("/{id}/requests/{userId}")
    public ResponseEntity<String> updateRequestJoinStatus(
        @PathVariable String id,
        @PathVariable String userId,
        @RequestHeader("userId") String requestingUserId,
        @RequestParam(value = "status") String status
    ) {
		Optional<RequestJoinGroupModel> optionalRequest = requestJoinGroupRepository.findByGroupIdAndUserId(id, userId);
		if (!optionalRequest.isPresent()) {
		    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Request not found");
		}
		
		RequestJoinGroupModel request = optionalRequest.get();
		if (status.equalsIgnoreCase("approved")) {
		    // Add group member
			GroupMemberModel member = new GroupMemberModel();
			GroupUserId groupUserId = new GroupUserId();
			groupUserId.setGroup(new GroupModel(id));
			groupUserId.setUser(new UserModel(userId));
			member.setId(groupUserId);
			member.setCreateAt(new Date());
			member.setRole(GroupMemberRole.MEMBER);
			groupMemberRepository.save(member);
			groupRepository.participantCountIncrement(id, 1);
		} 
		
		requestJoinGroupRepository.deleteRequestJoin(id, userId);
		
		return ResponseEntity.status(HttpStatus.OK).body("Request status updated successfully");
    }
    
	@PreAuthorize("0 == @groupRepository.isPrivate(#id) or 1 == @groupMemberRepository.isMember(#id, #requestingUserId)")
    @GetMapping("{id}/posts")
    public ResponseEntity<HashMap<String, Object>> searchGroupPosts(
    		@PathVariable String id,
    		@RequestHeader("userId") String requestingUserId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "title", required = false, defaultValue = "") String title,
            @RequestParam(value = "tagsId", required = false) List<Integer> tagsId,
            @RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId) {
    	Optional<GroupModel> optionalGroup = groupRepository.findById(id);
	    GroupModel group = optionalGroup.get();
	    Optional<GroupMemberModel> existingMemberOptional = groupMemberRepository.findByGroupIdAndUserId(id, requestingUserId);
        
        if (pageSize == 0 || pageSize > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        HashMap<String, Object> result = new HashMap<>();
        
    	boolean canDelete = false;
		if (1 == groupMemberRepository.hasGroupMemberRole(id, requestingUserId, "CREATOR") || 
				1 == groupMemberRepository.hasGroupMemberRole(id, requestingUserId, "ADMIN")) {
			canDelete = true;
		}
        
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<IPostGroupDto> posts = postGroupRepository.searchGroupPosts(id, title, requestingUserId, tagsId, statusId, canDelete, pageable);

        result.put("totalPages", posts.getTotalPages());
        result.put("posts", posts.getContent());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

	@PreAuthorize("0 == @postGroupRepository.isPrivateByPostId(#postId) or 1 == @postGroupRepository.isMemberByPostId(#postId, #requestingUserId)")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<IPostGroupDto> getGroupPostById(
    		@PathVariable String postId, 
    		@RequestHeader("userId") String requestingUserId) {
		boolean canDelete = false;
		if (1 == postGroupRepository.hasGroupMemberRoleByPostId(postId, requestingUserId, "CREATOR") || 
				1 == postGroupRepository.hasGroupMemberRoleByPostId(postId, requestingUserId, "ADMIN")) {
			canDelete = true;
		}
		
        Optional<IPostGroupDto> optionalPost = postGroupRepository.findPostById(postId, requestingUserId, canDelete);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
        Optional<GroupModel> optionalGroup = groupRepository.findById(optionalPost.get().getGroupId());
        if (optionalGroup.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	    }
	    GroupModel group = optionalGroup.get();
	    Optional<GroupMemberModel> existingMemberOptional = groupMemberRepository.findByGroupIdAndUserId(optionalPost.get().getGroupId(), requestingUserId);
	    if (group.getPrivacy().equals(Privacy.PRIVATE) && 
	    		!(existingMemberOptional.isPresent() && !existingMemberOptional.get().isDelete())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(optionalPost.get());
    }

	@PreAuthorize("1 == @groupMemberRepository.isMember(#id, #creator)")
    @PostMapping("{id}/posts")
    public ResponseEntity<String> addGroupPost(@PathVariable String id, //group id
    		@RequestHeader("userId") String creator,
    		@RequestBody PostGroupModel reqPostModel) {

    	String postId = UUID.randomUUID().toString();
    	PostGroupModel newPost = new PostGroupModel(id, creator, reqPostModel.getTitle(), reqPostModel.getContent(), reqPostModel.getTags());
    	newPost.setPublishedAt(new Date());
    	
        postGroupRepository.save(newPost);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPost.getId());
    }

	@PreAuthorize("1 == @postGroupRepository.isGroupPostOwner(#postId, #creator)")
    @PutMapping("/posts/{postId}")
    public ResponseEntity<String> updateGroupPost(
    		@PathVariable String postId, 
    		@RequestHeader("userId") String creator,
    		@RequestBody PostGroupModel reqPostModel) {
        Optional<PostGroupModel> optionalPost = postGroupRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        PostGroupModel post = optionalPost.get();
        
        if (!reqPostModel.getTitle().isEmpty() && reqPostModel.getTitle() != null) {
        	post.setTitle(reqPostModel.getTitle());
        }
        if (!reqPostModel.getContent().isEmpty() && reqPostModel.getContent() != null) {
        	post.setContent(reqPostModel.getContent());
        }
        if (reqPostModel.getTags() != null) {
        	post.setTags(reqPostModel.getTags());
        }
        if (reqPostModel.getStatus() != null) {
        	post.setStatus(reqPostModel.getStatus());
        }
        
        postGroupRepository.save(post);
        
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    
	@PreAuthorize("1 == @postGroupRepository.isGroupPostOwner(#id, #creator)")
	@PutMapping("/posts/{id}/images")
	public ResponseEntity<String> createPostGroupImages(@RequestHeader("userId") String creator,
			@PathVariable String id,
			@RequestParam(value = "addedImages", required = false) List<MultipartFile> addedImages,
			@RequestParam(value = "deletedImageIds", required = false) List<String> deletedImageIds) {
		if (addedImages == null && deletedImageIds == null) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		Optional<PostGroupModel> optionalPostGroup = postGroupRepository.findById(id);
		if (optionalPostGroup.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid post id");
		}

		PostGroupModel postGroup = optionalPostGroup.get();
		List<PicturePostGroupModel> images = postGroup.getPictures();

		if (addedImages != null && deletedImageIds != null
				&& images.size() + addedImages.size() - deletedImageIds.size() > MAX_IMAGE_SIZE_PER_POST) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exceed images can be uploaded per post");
		}

		// Delete images
		if (deletedImageIds != null && deletedImageIds.size() != 0) {
			List<PicturePostGroupModel> deletedImages = new ArrayList<PicturePostGroupModel>();
			for (PicturePostGroupModel image : images) {
				if (deletedImageIds.contains(image.getId())) {
					try {
						boolean successful = imageUtils.deleteImageFromStorageByUrl(image.getPictureUrl());
						if (successful) {
							deletedImages.add(image);
						} else {
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
									.body("Error deleting images");
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting images");
					} catch (IOException e) {
						e.printStackTrace();
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting images");
					}

				}
			}
			// Remove deleted images from list
			if (deletedImages.size() != 0) {
				images.removeAll(deletedImages);
			}
			// Update picture order after deleting
			for (int i = 0; i < images.size(); i++) {
				images.get(i).setPitctureOrder(i);
			}
		}
		int imagesSize = images.size();

		// Add new images
		if (addedImages != null && addedImages.size() != 0) {
			try {
				for (int i = 0; i < addedImages.size(); i++) {
					int order = imagesSize + i;
					String pictureId = UUID.randomUUID().toString();
					String pictureUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id),
							addedImages.get(i),
							pictureId);
					postGroup.getPictures()
							.add(new PicturePostGroupModel(pictureId, postGroup, pictureUrl, order));
				}
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving images");
			}
		}

		postGroupRepository.save(postGroup);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("1 == @postGroupRepository.hasGroupMemberRoleByPostId(#postId, #creator, \"CREATOR\") or "
			+ "1 == @postGroupRepository.hasGroupMemberRoleByPostId(#postId, #creator, \"ADMIN\") or "
			+ "1 == @postGroupRepository.isGroupPostOwner(#postId, #creator)")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deleteGroupPost(
    		@PathVariable String postId,
    		@RequestHeader("userId") String creator) {
        Optional<PostGroupModel> optionalPost = postGroupRepository.findById(postId);
        PostGroupModel post = optionalPost.get();
        post.getPictures().clear();
        post.setStatus(new StatusPostModel(4));
        postGroupRepository.save(post);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    
    @GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getPostComments(
			@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		boolean canDelete = false;
		if (1 == postGroupRepository.hasGroupMemberRoleByPostId(id, userId, "CREATOR") || 
				1 == postGroupRepository.hasGroupMemberRoleByPostId(id, userId, "ADMIN")) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostGroupDto> comments = commentPostGroupRepository.getComments(id, userId, canDelete, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getPostChildrenComments(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentPostGroupModel> parentComment = commentPostGroupRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		boolean canDelete = false;
		if (1 == commentPostGroupRepository.hasGroupMemberRoleByCommentId(commentId, userId, "CREATOR") || 
				1 == commentPostGroupRepository.hasGroupMemberRoleByCommentId(commentId, userId, "ADMIN")) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostGroupDto> comments = commentPostGroupRepository.getChildrenComment(commentId, userId, canDelete, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("1 == @postGroupRepository.isMemberByPostId(#id, #creator)")
	@PostMapping("/{id}/comments")
	public ResponseEntity<String> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentPostGroupModel comment) {
		comment.setId(UUID.randomUUID().toString());
		comment.setPostGroup(new PostGroupModel(id));
		comment.setCreator(new UserModel(creator));
		commentPostGroupRepository.save(comment);

		if (comment.getParentId() != null) {
			commentPostGroupRepository.commentCountIncrement(comment.getParentId(), 1);
		} else {
			postGroupRepository.commentCountIncrement(id, 1);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("1 == @commentPostGroupRepository.isCommentOwner(#commentId, #userId)")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId, @RequestBody CommentPostGroupModel updatedComment) {
		if (updatedComment.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}
		commentPostGroupRepository.updateComment(commentId, userId, updatedComment.getContent());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("1 == @commentPostGroupRepository.hasGroupMemberRoleByCommentId(#commentId, #creator, \"CREATOR\") or "
			+ "1 == @commentPostGroupRepository.hasGroupMemberRoleByCommentId(#commentId, #creator, \"ADMIN\") or "
			+ "1 == @commentPostGroupRepository.isCommentOwner(#commentId, #creator)")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentPostGroupModel> optionalComment = commentPostGroupRepository.findById(commentId);
		CommentPostGroupModel originalComment = optionalComment.get();

		// Initilize variables
		List<CommentPostGroupModel> childrenComments = new ArrayList<CommentPostGroupModel>();
		List<String> allParentId = new ArrayList<String>();
		
		// Get children comments
		String curCommentId = commentId;
		allParentId.add(curCommentId);
		childrenComments.addAll(commentPostGroupRepository.getChildrenComment(curCommentId));
		
		// Start the loop
		while (!childrenComments.isEmpty()) {
			CommentPostGroupModel curComment = childrenComments.get(0);
			curCommentId = curComment.getId();
			List<CommentPostGroupModel> temp = commentPostGroupRepository.getChildrenComment(curCommentId);
			if (!temp.isEmpty()) {
				allParentId.add(curCommentId);
				childrenComments.addAll(temp);
			}
		
			childrenComments.remove(0);
		}
		
		// Delete all comments and update comment count
		int deleted = commentPostGroupRepository.deleteComment(commentId);
		for (String parentId : allParentId) {
			commentPostGroupRepository.deleteChildrenComment(parentId);
		}
		if (deleted != 0) {
			if (originalComment.getParentId() != null) {
				commentPostGroupRepository.commentCountIncrement(originalComment.getParentId(), -1);
			} else {
				postGroupRepository.commentCountIncrement(originalComment.getPostGroup().getId(), -1);
			}
		}
		

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	@GetMapping("/{id}/react")
	public ResponseEntity<HashMap<String, Object>> getPostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestParam Integer reactId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString("desc"), "createAt"));
			Page<IInteractPostGroupDto> users = interactPostGroupRepository.getReactionUsers(id, reactId,
					pageable);

			result.put("totalPages", users.getTotalPages());
			result.put("users", users.getContent());
		} catch (IllegalArgumentException | UnknownPathException | InvalidDataAccessApiUsageException e) {
			System.err.println(e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			System.err.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PostMapping("/{id}/react")
	public ResponseEntity<String> postPostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		Optional<InteractPostGroupModel> optionalInteractPostGroup = interactPostGroupRepository
				.findById(new InteractPostGroupId(id, creatorId));

		if (!optionalInteractPostGroup.isEmpty() && optionalInteractPostGroup.get().getIsDelete() == false) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have already reacted to this post");
		}

		InteractPostGroupModel interactPostGroup = new InteractPostGroupModel(id, creatorId,
				req.getReactId());
		interactPostGroupRepository.save(interactPostGroup);
		postGroupRepository.reactionCountIncrement(id, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PutMapping("/{id}/react")
	public ResponseEntity<String> putPostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		Optional<InteractPostGroupModel> optionalInteractPostGroup = interactPostGroupRepository
				.findById(new InteractPostGroupId(id, creatorId));

		if (optionalInteractPostGroup.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not found");
		}

		InteractPostGroupModel interactPostGroup = optionalInteractPostGroup.get();
		if (interactPostGroup.getReact().getId() == req.getReactId()) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		interactPostGroup.setReact(new ReactModel(req.getReactId()));
		interactPostGroupRepository.save(interactPostGroup);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@DeleteMapping("/{id}/react")
	public ResponseEntity<String> deletePostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id) {
		Optional<InteractPostGroupModel> optionalInteractPostGroup = interactPostGroupRepository
				.findById(new InteractPostGroupId(id, creatorId));

		if (optionalInteractPostGroup.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not found");
		}

		InteractPostGroupModel interactPostGroup = optionalInteractPostGroup.get();
		interactPostGroup.setIsDelete(true);
		postGroupRepository.reactionCountIncrement(id, -1);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
