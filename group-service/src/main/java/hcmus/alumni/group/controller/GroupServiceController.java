package hcmus.alumni.group.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.group.common.Privacy;
import hcmus.alumni.group.common.Role;
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
import hcmus.alumni.group.utils.ImageUtils;
import hcmus.alumni.group.dto.IGroupDto;
import hcmus.alumni.group.dto.IGroupMemberDto;
import hcmus.alumni.group.dto.IRequestJoinGroupDto;
import hcmus.alumni.group.dto.IPostGroupDto;
import hcmus.alumni.group.dto.ICommentPostGroupDto;
import hcmus.alumni.group.repository.GroupRepository;
import hcmus.alumni.group.repository.GroupMemberRepository;
import hcmus.alumni.group.repository.RequestJoinGroupRepository;
import hcmus.alumni.group.repository.PostGroupRepository;
import hcmus.alumni.group.repository.CommentPostGroupRepository;
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
            @RequestParam(value = "cover") MultipartFile cover,
            @RequestParam(value = "statusId", required = false, defaultValue = "1") Integer statusId
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
            groupModel.setCreator(new UserModel(creatorId));
            groupModel.setAvatarUrl(avatarUrl);
            groupModel.setCoverUrl(coverUrl);
            groupModel.setStatus(new StatusUserGroupModel(statusId));

            groupRepository.save(groupModel);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save images");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('Admin')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateGroup(@PathVariable String id,
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "type", required = false, defaultValue = "") String type,
            @RequestParam(value = "website", required = false, defaultValue = "") String website,
            @RequestParam(value = "privacy", required = false, defaultValue = "") Privacy privacy,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "statusId", required = false) Integer statusId
	) {
        try {
    		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
            if (optionalGroup.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Group not found");
            }

            GroupModel groupModel = optionalGroup.get();
            boolean isPut = false;
            
            if (!name.isEmpty()) {
                groupModel.setName(name);
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

            if (avatar != null) {
                String avatarUrl = imageUtils.saveImageToStorage(imageUtils.getGroupPath(id), avatar, "avatar");
                groupModel.setAvatarUrl(avatarUrl);
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

	@PreAuthorize("hasAnyAuthority('Admin')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteGroup(@PathVariable String id) {
		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
	    if (optionalGroup.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
	    }
	    GroupModel group = optionalGroup.get();
	    group.setStatus(new StatusUserGroupModel(3));
	    groupRepository.save(group);
	    return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	@GetMapping("/{id}/members")
	public ResponseEntity<HashMap<String, Object>> getGroupMembersByGroupId(
			@PathVariable String id,
	    @RequestParam(value = "page", defaultValue = "0") int page,
	    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
	) {
		
		if (pageSize == 0 || pageSize > 50) {
		    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		
		HashMap<String, Object> result = new HashMap<>();
		
		Pageable pageable = PageRequest.of(page, pageSize);
	    Page<IGroupMemberDto> members = groupMemberRepository.searchMembers(id, pageable);
	
	    result.put("totalPages", members.getTotalPages());
	    result.put("members", members.getContent());
	    
	    return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@PutMapping("/{id}/members/{userId}")
    public ResponseEntity<String> updateGroupMemberRole(
    		@PathVariable String id, 
    		@PathVariable String userId,
    		@RequestBody GroupMemberModel updatedGroupMember) {
		if (updatedGroupMember.getRole() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}
		int updates = groupMemberRepository.updateGroupMember(id, userId, updatedGroupMember.getRole());
		if (updates == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<String> deleteGroupMember(@PathVariable String id, @PathVariable String userId) {
    	Optional<GroupMemberModel> optionalGroupMember = groupMemberRepository.findByGroupIdAndUserId(id, userId);
        GroupMemberModel groupMember = optionalGroupMember.get();
        groupMember.setIsDelete(true);
        groupMemberRepository.save(groupMember);
        return ResponseEntity.ok("Group member deleted successfully");
    }
    
    @GetMapping("/{id}/requests")
    public ResponseEntity<HashMap<String, Object>> getRequestJoinsByGroupId(
        @PathVariable String id,
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
    	RequestJoinGroupModel newRequestJoin = new RequestJoinGroupModel();
    	GroupUserId groupUserId = new GroupUserId();
        groupUserId.setGroupId(id);
        groupUserId.setUserId(userId);
        newRequestJoin.setId(groupUserId);
        requestJoinGroupRepository.save(newRequestJoin);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    //admin or mod
    @PutMapping("/{id}/requests/{userId}")
    public ResponseEntity<String> updateRequestJoinStatus(
        @PathVariable String id,
        @PathVariable String userId,
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
            groupUserId.setGroupId(id);
            groupUserId.setUserId(userId);
            member.setId(groupUserId);
            member.setRole(Role.MEMBER);
            groupMemberRepository.save(member);
        } 
        
        request.setIsDelete(true);
        requestJoinGroupRepository.save(request);
        return ResponseEntity.status(HttpStatus.OK).body("Request status updated successfully");
    }
    
    @GetMapping("/posts")
    public ResponseEntity<HashMap<String, Object>> searchGroupPosts(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "title", required = false, defaultValue = "") String title,
            @RequestParam(value = "tagsId", required = false) List<Integer> tagsId,
            @RequestParam(value = "statusId", required = false) Integer statusId) {
        if (pageSize == 0 || pageSize > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        HashMap<String, Object> result = new HashMap<>();
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<IPostGroupDto> posts = postGroupRepository.searchGroupPosts(title, tagsId, statusId, pageable);

        result.put("totalPages", posts.getTotalPages());
        result.put("posts", posts.getContent());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<IPostGroupDto> getGroupPostById(@PathVariable String postId) {
        Optional<IPostGroupDto> optionalPost = postGroupRepository.findPostById(postId);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(optionalPost.get());
    }

    //admin or mod or creator of post
    @PostMapping("{id}/posts")
    public ResponseEntity<String> addGroupPost(@PathVariable String id, //group id
    		@RequestHeader("userid") String creator,
    		@RequestParam(value = "title", required = false, defaultValue = "") String title, 
	        @RequestParam(value = "content", required = false, defaultValue = "") String content,
	        @RequestParam(value = "tagsId", required = false, defaultValue = "") Integer[] tagsId,
	        @RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId) {

    	String postId = UUID.randomUUID().toString();
    	PostGroupModel newPost = new PostGroupModel();
    	newPost.setId(postId);
    	newPost.setCreator(new UserModel(creator));
    	newPost.setGroupId(id);
    	newPost.setTitle(title);
    	newPost.setContent(content);
    	newPost.setTags(tagsId);
    	newPost.setStatus(new StatusPostModel(statusId));
        postGroupRepository.save(newPost);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    //admin or mod or creator of post
    @PutMapping("/posts/{postId}")
    public ResponseEntity<String> updateGroupPost(
    		@PathVariable String postId, 
    		@RequestParam(value = "title", required = false, defaultValue = "") String title, 
	        @RequestParam(value = "content", required = false, defaultValue = "") String content,
	        @RequestParam(value = "tagsId", required = false) Integer[] tagsId,
	        @RequestParam(value = "statusId", required = false) Integer statusId) {
        Optional<PostGroupModel> optionalPost = postGroupRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        PostGroupModel post = optionalPost.get();
        boolean isPut = false;
        
        if (!title.isEmpty()) {
        	post.setTitle(title);
            isPut = true;
        }
        if (!content.isEmpty()) {
        	post.setContent(content);
            isPut = true;
        }
        if (tagsId != null) {
        	post.setTags(tagsId);
            isPut = true;
        }
        if (statusId != null) {
        	post.setStatus(new StatusPostModel(statusId));
            isPut = true;
        }
        
        if (isPut)
        	postGroupRepository.save(post);
        return ResponseEntity.status(HttpStatus.OK).body("Post updated successfully");
    }

    //admin or mod or creator of post
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deleteGroupPost(@PathVariable String postId) {
        Optional<PostGroupModel> optionalPost = postGroupRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        PostGroupModel post = optionalPost.get();
        post.setStatus(new StatusPostModel(4));
        postGroupRepository.save(post);
        return ResponseEntity.status(HttpStatus.OK).body("Post deleted successfully");
    }
    
    @GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getPostComments(@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostGroupDto> comments = commentPostGroupRepository.getComments(id, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get children comments of a comment
	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getPostChildrenComments(
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

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostGroupDto> comments = commentPostGroupRepository.getChildrenComment(commentId, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<String> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentPostGroupModel comment) {
		comment.setId(UUID.randomUUID().toString());
		comment.setPostGroup(new PostGroupModel(id));
		comment.setCreator(new UserModel(creator));
		System.out.println(comment.toString());

		if (comment.getParentId() != null) {
			commentPostGroupRepository.commentCountIncrement(comment.getParentId(), 1);
		}

		commentPostGroupRepository.save(comment);
		postGroupRepository.commentCountIncrement(id, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId, @RequestBody CommentPostGroupModel updatedComment) {
		if (updatedComment.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}

		// Check if user is comment's creator
		Optional<CommentPostGroupModel> optionalComment = commentPostGroupRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
		}
		CommentPostGroupModel comment = optionalComment.get();
		if (!comment.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this comment");
		}

		commentPostGroupRepository.updateComment(commentId, creator, updatedComment.getContent());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentPostGroupModel> originalComment = commentPostGroupRepository.findById(commentId);
		if (originalComment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		// Check if user is comment's creator
		if (!originalComment.get().getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this comment");
		}

		String postId = originalComment.get().getPostGroup().getId();
		if (originalComment.get().getParentId() != null) {
			commentPostGroupRepository.commentCountIncrement(originalComment.get().getParentId(), -1);
		}

		// Initilize variables
		List<CommentPostGroupModel> childrenComments = new ArrayList<CommentPostGroupModel>();
		List<String> allParentId = new ArrayList<String>();
		int totalDelete = 1;

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
		commentPostGroupRepository.deleteComment(commentId, creator);
		for (String parentId : allParentId) {
			totalDelete += commentPostGroupRepository.deleteChildrenComment(parentId);
		}
		postGroupRepository.commentCountIncrement(postId, -totalDelete);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
