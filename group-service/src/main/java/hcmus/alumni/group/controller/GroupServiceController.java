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
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;

import org.modelmapper.ModelMapper;
import org.hibernate.query.sqm.UnknownPathException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import hcmus.alumni.group.repository.notification.EntityTypeRepository;
import hcmus.alumni.group.repository.notification.NotificationChangeRepository;
import hcmus.alumni.group.repository.notification.NotificationObjectRepository;
import hcmus.alumni.group.repository.notification.NotificationRepository;
import hcmus.alumni.group.utils.FirebaseService;
import hcmus.alumni.group.utils.NotificationService;
import hcmus.alumni.group.common.GroupMemberRole;
import hcmus.alumni.group.exception.AppException;
import hcmus.alumni.group.model.GroupModel;
import hcmus.alumni.group.model.UserModel;
import hcmus.alumni.group.model.StatusPostModel;
import hcmus.alumni.group.model.StatusUserGroupModel;
import hcmus.alumni.group.model.GroupUserId;
import hcmus.alumni.group.model.GroupMemberModel;
import hcmus.alumni.group.model.RequestJoinGroupModel;
import hcmus.alumni.group.model.PostGroupModel;
import hcmus.alumni.group.model.CommentPostGroupModel;
import hcmus.alumni.group.model.PicturePostGroupModel;
import hcmus.alumni.group.model.InteractPostGroupId;
import hcmus.alumni.group.model.InteractPostGroupModel;
import hcmus.alumni.group.model.ReactModel;
import hcmus.alumni.group.model.TagModel;
import hcmus.alumni.group.model.UserVotePostGroupId;
import hcmus.alumni.group.model.UserVotePostGroupModel;
import hcmus.alumni.group.model.VoteOptionPostGroupModel;
import hcmus.alumni.group.utils.ImageUtils;
import hcmus.alumni.group.common.NotificationType;
import hcmus.alumni.group.model.notification.EntityTypeModel;
import hcmus.alumni.group.model.notification.NotificationChangeModel;
import hcmus.alumni.group.model.notification.NotificationModel;
import hcmus.alumni.group.model.notification.NotificationObjectModel;
import hcmus.alumni.group.model.notification.StatusNotificationModel;
import hcmus.alumni.group.dto.response.IGroupDto;
import hcmus.alumni.group.dto.response.IGroupMemberDto;
import hcmus.alumni.group.dto.response.IRequestJoinGroupDto;
import hcmus.alumni.group.dto.response.PostGroupDto;
import hcmus.alumni.group.dto.response.ICommentPostGroupDto;
import hcmus.alumni.group.dto.response.IInteractPostGroupDto;
import hcmus.alumni.group.dto.response.IUserDto;
import hcmus.alumni.group.dto.response.IUserVotePostGroupDto;
import hcmus.alumni.group.dto.request.PostGroupRequestDto;
import hcmus.alumni.group.dto.request.ReactRequestDto;
import hcmus.alumni.group.dto.request.PostGroupRequestDto.TagRequestDto;
import hcmus.alumni.group.dto.request.PostGroupRequestDto.VoteRequestDto;
import hcmus.alumni.group.repository.UserRepository;
import hcmus.alumni.group.repository.GroupRepository;
import hcmus.alumni.group.repository.GroupMemberRepository;
import hcmus.alumni.group.repository.RequestJoinGroupRepository;
import hcmus.alumni.group.repository.PostGroupRepository;
import hcmus.alumni.group.repository.CommentPostGroupRepository;
import hcmus.alumni.group.repository.InteractPostGroupRepository;
import hcmus.alumni.group.repository.TagRepository;
import hcmus.alumni.group.repository.VoteOptionPostGroupRepository;
import hcmus.alumni.group.repository.UserVotePostGroupRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@RequestMapping("/groups")
public class GroupServiceController {
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	@PersistenceContext
	private EntityManager em;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private GroupMemberRepository groupMemberRepository;
	@Autowired
	private RequestJoinGroupRepository requestJoinGroupRepository;
	@Autowired
	private PostGroupRepository postGroupRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private CommentPostGroupRepository commentPostGroupRepository;
	@Autowired
	private InteractPostGroupRepository interactPostGroupRepository;
	@Autowired
	private VoteOptionPostGroupRepository voteOptionPostGroupRepository;
	@Autowired
	private UserVotePostGroupRepository userVotePostGroupRepository;
	@Autowired
	private EntityTypeRepository entityTypeRepository;  
	@Autowired
	private NotificationObjectRepository notificationObjectRepository; 
	@Autowired
	private NotificationChangeRepository notificationChangeRepository;
	@Autowired
	private NotificationRepository notificationRepository;
	@Autowired
	private ImageUtils imageUtils;
	@Autowired
	private FirebaseService firebaseService;
	@Autowired
	private NotificationService notificationService;
	
	private final static int MAXIMUM_PAGES = 50;
	private final static int MAXIMUM_TAGS = 5;
	private final static int MAXIMUM_VOTE_OPTIONS = 10;
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
			@RequestParam(value = "tagNames", required = false) List<String> tagNames,
			@RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId,
			@RequestParam(value = "privacy", required = false) Privacy privacy,
			@RequestParam(value = "isJoined", required = false) Boolean isJoined) {

		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		
		HashMap<String, Object> result = new HashMap<>();
		if (tagNames != null) {
			for (int i = 0; i < tagNames.size(); i++) {
				tagNames.set(i, TagModel.sanitizeTagName(tagNames.get(i)));
			}
		}
		
		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Group.Delete"))) {
			canDelete = true;
		}
		
		try {
		    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
		    Page<IGroupDto> groups = groupRepository.searchGroups(name, tagNames, statusId, privacy, isJoined, requestingUserId, canDelete, pageable);
		
		    result.put("totalPages", groups.getTotalPages());
		    result.put("groups", groups.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(70100, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(70101, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
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
	    	throw new AppException(70200, "Không tìm thấy nhóm", HttpStatus.NOT_FOUND);
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
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam(value = "tagNames", required = false) List<String> tagNames,
            @RequestParam(value = "statusId", required = false, defaultValue = "2") Integer statusId
	) {
		if (name.equals("")) {
			throw new AppException(70400, "Name không được để trống", HttpStatus.BAD_REQUEST);
        }
		if (tagNames != null && tagNames.size() > MAXIMUM_TAGS) {
			throw new AppException(70402, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
		
        String id = UUID.randomUUID().toString();
        try {
            String coverUrl = null;

            // Save cover image
            if (cover == null || cover.isEmpty()) {
            	coverUrl = imageUtils.getDefaultCoverUrl();
            } else {
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
            if (tagNames != null && !tagNames.isEmpty()) {
				Set<TagModel> tags = new HashSet<TagModel>();
				for (String tagName : tagNames) {
					var sanitizedTagName = TagModel.sanitizeTagName(tagName);
					if (sanitizedTagName.isBlank()) {
						continue;
					}
					TagModel tag = tagRepository.findByName(sanitizedTagName);
					if (tag == null) {
						tag = new TagModel(sanitizedTagName);
					}
					tags.add(tag);
				}
				groupModel.setTags(tags);
			}
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
        	throw new AppException(70401, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
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
            @RequestParam(value = "tagNames", required = false) List<String> tagNames,
            @RequestParam(value = "statusId", required = false) Integer statusId
	) {
		if (tagNames != null && tagNames.size() > MAXIMUM_TAGS) {
			throw new AppException(70502, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
        try {
    		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
    		if (optionalGroup.isEmpty()) {
    	    	throw new AppException(70500, "Không tìm thấy nhóm", HttpStatus.NOT_FOUND);
    	    }
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
            
            if (tagNames != null) {
				Set<TagModel> currentTags = groupModel.getTags();
				Set<TagModel> updatedTags = new HashSet<TagModel>();

				for (String tagName : tagNames) {
					var sanitizedTagName = TagModel.sanitizeTagName(tagName);
					if (sanitizedTagName.isBlank()) {
						continue;
					}
					updatedTags.add(new TagModel(sanitizedTagName));
				}
				// Remove tags
				for (Iterator<TagModel> iterator = currentTags.iterator(); iterator.hasNext();) {
					TagModel tag = iterator.next();
					if (!updatedTags.contains(tag)) {
						iterator.remove();
					}
				}
				// Add tags
				for (TagModel tag : updatedTags) {
					if (!currentTags.contains(tag)) {
						TagModel find = tagRepository.findByName(tag.getName());
						if (find == null) {
							find = new TagModel(tag.getName());
						}
						currentTags.add(find);
					}
				}
				groupModel.setTags(currentTags);
				isPut = true;
			}
            
            if (statusId != null) {
            	groupModel.setStatus(new StatusUserGroupModel(statusId));
                isPut = true;
            }
            if (isPut) {
            	groupRepository.save(groupModel);
            	
				// Fetch group members
				Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
				Page<GroupMemberModel> groupMembers = groupMemberRepository.getMembers(id, null, pageable);
				
				// Ensure Notification Object for group update exists
				EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType("group", NotificationType.UPDATE)
				        .orElseGet(() -> {
				            EntityTypeModel newEntityType = new EntityTypeModel();
				            newEntityType.setEntityTable("group");
				            newEntityType.setNotificationType(NotificationType.UPDATE);
				            return entityTypeRepository.save(newEntityType);
				        });
				
				NotificationObjectModel notificationObject = new NotificationObjectModel();
				notificationObject.setEntityType(entityType);
				notificationObject.setEntityId(id);
				notificationObjectRepository.save(notificationObject);
				
				// Create Notification Change
				NotificationChangeModel notificationChange = new NotificationChangeModel();
				notificationChange.setNotificationObject(notificationObject);
				notificationChange.setActor(new UserModel(requestingUserId));
				notificationChangeRepository.save(notificationChange);
				
				// Create notifications for each group member
				for (GroupMemberModel member : groupMembers.getContent()) {
				    NotificationModel notification = new NotificationModel();
				    notification.setNotificationObject(notificationObject);
				    notification.setNotifier(member.getUser());
				    notification.setStatus(new StatusNotificationModel(1));
				    notificationRepository.save(notification);
				
				    // Send push notification
				    String notificationMessage = "Nhóm " + groupModel.getName() + " mà bạn tham gia đã được cập nhật thông tin";
				    firebaseService.sendNotification(
				    		notification, notificationChange, notificationObject,
				    		groupModel.getCoverUrl(),
				    		notificationMessage, 
				    		null);
				}
            }
        } catch (IOException e) {
        	throw new AppException(70501, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
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
		if (optionalGroup.isEmpty()) {
	    	throw new AppException(70600, "Không tìm thấy nhóm", HttpStatus.NOT_FOUND);
	    }
	    GroupModel group = optionalGroup.get();
	    group.setStatus(new StatusUserGroupModel(3));
	    groupRepository.save(group);
	    
	    groupMemberRepository.deleteAllGroupMember(id);
	    
		List<String> entityIds = commentPostGroupRepository.findByGroupId(id);
		entityIds.addAll(postGroupRepository.findByGroupId(id));
		entityIds.add(id);
		notificationService.deleteNotificationsByEntityIds(entityIds);

	    return ResponseEntity.status(HttpStatus.OK).body("");
	}
	
	@GetMapping("/{id}/members")
	public ResponseEntity<HashMap<String, Object>> getGroupMembersByGroupId(
			@PathVariable String id,
			@RequestParam(value = "name", required = false, defaultValue = "") String name,
		    @RequestParam(value = "page", defaultValue = "0") int page,
		    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
		    @RequestParam(value = "role", required = false) GroupMemberRole role
	) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		
		HashMap<String, Object> result = new HashMap<>();
		
		Pageable pageable = PageRequest.of(page, pageSize);
	    Page<IGroupMemberDto> members = groupMemberRepository.searchMembers(id, name, role, pageable);
	
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
		if (requestingUserId.equals(userId)) {
			throw new AppException(70800, "Không thể thay đổi vai trò của bản thân", HttpStatus.BAD_REQUEST);
		}
		if (updatedGroupMember.getRole() == null) {
			throw new AppException(70801, "Role không được để trống", HttpStatus.BAD_REQUEST);
		}
		int updates = groupMemberRepository.updateGroupMember(id, userId, updatedGroupMember.getRole());
		if (updates == 0) {
			throw new AppException(70802, "Không tìm thấy thành viên trong nhóm", HttpStatus.NOT_FOUND);
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
    	int delete = groupMemberRepository.deleteGroupMember(id, userId);
    	
    	if (delete != 0) {
    		groupRepository.participantCountIncrement(id, -1);
    		return ResponseEntity.status(HttpStatus.OK).body(null);
    	}
    	else
    		throw new AppException(70900, "Không tìm thấy thành viên trong nhóm", HttpStatus.NOT_FOUND);
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
        if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
        	pageSize = MAXIMUM_PAGES;
        }

        HashMap<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<IRequestJoinGroupDto> requests = requestJoinGroupRepository.searchRequestJoin(id, pageable);

        result.put("totalPages", requests.getTotalPages());
        result.put("requests", requests.getContent());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    
	@PreAuthorize("hasAnyAuthority('Group.Join')")
    @PostMapping("/{id}/requests")
    public ResponseEntity<String> addRequestJoin(
    		@PathVariable String id,
    		@RequestHeader("userId") String userId) {
    	Optional<GroupModel> optionalGroup = groupRepository.findById(id);
	    if (optionalGroup.isEmpty()) {
	    	throw new AppException(71100, "Không tìm thấy nhóm", HttpStatus.NOT_FOUND);
	    }
    	
    	// Check if the user is already a member of the group
        Optional<GroupMemberModel> existingMemberOptional = groupMemberRepository.findByGroupIdAndUserId(id, userId);
        if (existingMemberOptional.isPresent() && !existingMemberOptional.get().isDelete()) {
        	throw new AppException(71101, "Người dùng đã là thành viên của nhóm", HttpStatus.BAD_REQUEST);
        }

        // Check if there's a pending request to join the group
        Optional<RequestJoinGroupModel> pendingRequestOptional = requestJoinGroupRepository.findByGroupIdAndUserId(id, userId);
        if (pendingRequestOptional.isPresent() && !pendingRequestOptional.get().isDelete()) {
        	throw new AppException(71102, "Yêu cầu tham gia đang chờ xét duyệt", HttpStatus.BAD_REQUEST);
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
			
			// Fetch group members with roles CREATOR or ADMIN
			Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
			Page<GroupMemberModel> groupAdminsAndCreators = groupMemberRepository.getMembers(id, List.of(GroupMemberRole.CREATOR, GroupMemberRole.ADMIN), pageable);
			
			// Ensure Notification Object for join request exists
			EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType("request_join_group", NotificationType.CREATE)
			        .orElseGet(() -> {
			            EntityTypeModel newEntityType = new EntityTypeModel();
			            newEntityType.setEntityTable("request_join_group");
			            newEntityType.setNotificationType(NotificationType.CREATE);
			            return entityTypeRepository.save(newEntityType);
			        });
			
			NotificationObjectModel notificationObject = new NotificationObjectModel();
			notificationObject.setEntityType(entityType);
			notificationObject.setEntityId(id);
			notificationObjectRepository.save(notificationObject);
			
			// Create Notification Change
			NotificationChangeModel notificationChange = new NotificationChangeModel();
			notificationChange.setNotificationObject(notificationObject);
			notificationChange.setActor(new UserModel(userId));
			notificationChangeRepository.save(notificationChange);
			
			// Create notifications for each group admin/creator
			Optional<UserModel> optionalUser = userRepository.findById(userId);
			for (GroupMemberModel member : groupAdminsAndCreators.getContent()) {
			    NotificationModel notification = new NotificationModel();
			    notification.setNotificationObject(notificationObject);
			    notification.setNotifier(member.getUser());
			    notification.setStatus(new StatusNotificationModel(1));
			    notificationRepository.save(notification);
			
			    // Send push notification
			    String notificationMessage = optionalUser.get().getFullName() + " đã yêu cầu tham gia nhóm " + group.getName();
			    firebaseService.sendNotification(
			            notification, notificationChange, notificationObject,
			            group.getCoverUrl(),
			            notificationMessage,
			            null);
			}
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
			throw new AppException(71200, "Không tìm thấy yêu cầu tham gia", HttpStatus.NOT_FOUND);
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
		
		notificationService.deleteRequestJoinNotifications(id, userId);
		
		// Ensure Notification Object for join request exists
		EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType("request_join_group", NotificationType.UPDATE)
		        .orElseGet(() -> {
		            EntityTypeModel newEntityType = new EntityTypeModel();
		            newEntityType.setEntityTable("request_join_group");
		            newEntityType.setNotificationType(NotificationType.UPDATE);
		            return entityTypeRepository.save(newEntityType);
		        });
		
		NotificationObjectModel notificationObject = new NotificationObjectModel();
		notificationObject.setEntityType(entityType);
		notificationObject.setEntityId(id);
		notificationObjectRepository.save(notificationObject);
		
		// Create Notification Change
		NotificationChangeModel notificationChange = new NotificationChangeModel();
		notificationChange.setNotificationObject(notificationObject);
		notificationChange.setActor(new UserModel(requestingUserId));
		notificationChangeRepository.save(notificationChange);
		
		NotificationModel notification = new NotificationModel();
		notification.setNotificationObject(notificationObject);
		notification.setNotifier(new UserModel(userId));
		notification.setStatus(new StatusNotificationModel(1));
		notificationRepository.save(notification);
	
		// Send push notification
		Optional<GroupModel> optionalGroup = groupRepository.findById(id);
		GroupModel group = optionalGroup.get();
		String notificationMessage = "Yêu cầu tham gia nhóm " + group.getName() + " đã " + 
				(status.equalsIgnoreCase("approved") ? "được chấp thuận" : "bị từ chối");
		firebaseService.sendNotification(
		        notification, notificationChange, notificationObject,
		        group.getCoverUrl(),
		        notificationMessage,
		        null);
		
		return ResponseEntity.status(HttpStatus.OK).body(null);
    }
    
	@PreAuthorize("0 == @groupRepository.isPrivate(#id) or 1 == @groupMemberRepository.isMember(#id, #userId)")
    @GetMapping("{id}/posts")
	public ResponseEntity<HashMap<String, Object>> getPosts(
			@PathVariable String id, //group id
			@RequestHeader("userId") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "tagNames", required = false) List<String> tagNames) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (tagNames != null) {
			for (int i = 0; i < tagNames.size(); i++) {
				tagNames.set(i, TagModel.sanitizeTagName(tagNames.get(i)));
			}
		}

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (1 == groupMemberRepository.hasGroupMemberRole(id, userId, "CREATOR") || 
				1 == groupMemberRepository.hasGroupMemberRole(id, userId, "ADMIN")) {
			canDelete = true;
		}

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<PostGroupModel> postsPage = postGroupRepository.searchPostGroup(id, title, userId, tagNames, 
					canDelete, pageable);

			result.put("totalPages", postsPage.getTotalPages());
			List<PostGroupModel> postList = postsPage.getContent();
			List<Object[]> resultList = userVotePostGroupRepository.getVoteIdsByUserAndPosts(userId,
					postList.stream().map(PostGroupModel::getId).toList());

			// Create a map with key is post id and value is a set of vote ids
			HashMap<String, Set<Integer>> voteIdsMap = new HashMap<String, Set<Integer>>();
			for (Object[] obj : resultList) {
				Set<Integer> voteIds = voteIdsMap.get(String.valueOf(obj[0]));
				if (voteIds == null) {
					voteIds = new HashSet<Integer>();
				}
				voteIds.add(Integer.valueOf(String.valueOf(obj[1])));
				voteIdsMap.put(String.valueOf(obj[0]), voteIds);
			}

			// Set isVoted for each vote option
			for (PostGroupModel post : postList) {
				Set<Integer> voteIds = voteIdsMap.get(post.getId());
				if (voteIds == null) {
					continue;
				}
				for (VoteOptionPostGroupModel vote : post.getVotes()) {
					if (voteIds.contains(vote.getId().getVoteId())) {
						vote.setIsVoted(true);
					}
				}
			}

			result.put("posts", postList.stream().map(p -> mapper.map(p, PostGroupDto.class)).toList());
		} catch (IllegalArgumentException e) {
			throw new AppException(71300, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(71301, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("0 == @postGroupRepository.isPrivateByPostId(#id) or 1 == @postGroupRepository.isMemberByPostId(#id, #userId)")
    @GetMapping("/posts/{id}")
	public ResponseEntity<PostGroupDto> getPostById(@RequestHeader("userId") String userId, @PathVariable String id) {
		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (1 == postGroupRepository.hasGroupMemberRoleByPostId(id, userId, "CREATOR") || 
				1 == postGroupRepository.hasGroupMemberRoleByPostId(id, userId, "ADMIN")) {
			canDelete = true;
		}

		PostGroupModel post = postGroupRepository.findPostGroupById(id, userId, canDelete).orElse(null);

		if (post == null) {
			throw new AppException(71400, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}

		Set<Integer> voteIds = userVotePostGroupRepository.getVoteIdsByUserAndPost(userId, id);

		for (VoteOptionPostGroupModel vote : post.getVotes()) {
			if (voteIds.contains(vote.getId().getVoteId())) {
				vote.setIsVoted(true);
			}
		}

		return ResponseEntity.status(HttpStatus.OK).body(mapper.map(post, PostGroupDto.class));
	}

	@PreAuthorize("1 == @groupMemberRepository.isMember(#id, #creator)")
    @PostMapping("{id}/posts")
	public ResponseEntity<Map<String, Object>> createPostGroup(
			@PathVariable String id, //group id
			@RequestHeader("userId") String creator,
			@RequestBody PostGroupRequestDto reqPostGroup) {
		if (reqPostGroup.getTitle() == null || reqPostGroup.getTitle().isBlank()) {
			throw new AppException(71500, "Tiêu đề không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (reqPostGroup.getContent() == null || reqPostGroup.getContent().isBlank()) {
			throw new AppException(71501, "Nội dung không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (reqPostGroup.getTags() != null && reqPostGroup.getTags().size() > MAXIMUM_TAGS) {
			throw new AppException(71502, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
		if (reqPostGroup.getVotes() != null && reqPostGroup.getVotes().size() > MAXIMUM_VOTE_OPTIONS) {
			throw new AppException(71503, "Số lượng lựa chọn không được vượt quá " + MAXIMUM_VOTE_OPTIONS,
					HttpStatus.BAD_REQUEST);
		}

		// Handle tags
		Set<TagModel> tags = new HashSet<TagModel>();
		if (reqPostGroup.getTags() != null) {
			for (TagRequestDto reqTag : reqPostGroup.getTags()) {
				String sanitizedTagName = TagModel.sanitizeTagName(reqTag.getName());
				if (sanitizedTagName.isBlank()) {
					continue;
				}
				TagModel tag = tagRepository.findByName(sanitizedTagName);
				if (tag == null) {
					tag = new TagModel(sanitizedTagName);
				}
				tags.add(tag);
			}
		}

		PostGroupModel postGroup = new PostGroupModel(creator, reqPostGroup);
		postGroup.setGroupId(id);
		postGroup.setTags(tags);
		postGroupRepository.save(postGroup);
		return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("id", postGroup.getId()));
	}

	@PreAuthorize("1 == @postGroupRepository.isGroupPostOwner(#id, #userId)")
    @PutMapping("/posts/{id}")
	public ResponseEntity<String> updatePostGroup(
			@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestBody PostGroupRequestDto updatedPostGroup) {
		if (updatedPostGroup.getTags() != null && updatedPostGroup.getTags().size() > MAXIMUM_TAGS) {
			throw new AppException(71600, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
		PostGroupModel postGroup = postGroupRepository.findById(id).orElse(null);
		if (postGroup == null) {
			throw new AppException(71601, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		if (postGroup.getVotes().size() > 0) {
			throw new AppException(71602, "Không thể cập nhật bài viết đã có lựa chọn bình chọn",
					HttpStatus.BAD_REQUEST);
		}

		if (updatedPostGroup.getTitle() != null && !updatedPostGroup.getTitle().isBlank()) {
			postGroup.setTitle(updatedPostGroup.getTitle());
		}
		if (updatedPostGroup.getContent() != null && !updatedPostGroup.getContent().isBlank()) {
			postGroup.setContent(updatedPostGroup.getContent());
		}
		if (updatedPostGroup.getTags() != null) {
			Set<TagModel> currentTags = postGroup.getTags();
			Set<TagModel> updatedTags = new HashSet<TagModel>();

			for (var updatedTag : updatedPostGroup.getTags()) {
				var sanitizedTagName = TagModel.sanitizeTagName(updatedTag.getName());
				if (sanitizedTagName.isBlank()) {
					continue;
				}
				updatedTags.add(new TagModel(sanitizedTagName));
			}
			// Remove tags
			for (Iterator<TagModel> iterator = currentTags.iterator(); iterator.hasNext();) {
				TagModel tag = iterator.next();
				if (!updatedTags.contains(tag)) {
					iterator.remove();
				}
			}
			// Add tags
			for (TagModel tag : updatedTags) {
				if (!currentTags.contains(tag)) {
					TagModel find = tagRepository.findByName(tag.getName());
					if (find == null) {
						find = new TagModel(tag.getName());
					}
					currentTags.add(find);
				}
			}
			postGroup.setTags(currentTags);
		}

		postGroupRepository.save(postGroup);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
    
	@PreAuthorize("1 == @postGroupRepository.isGroupPostOwner(#id, #userId)")
	@PutMapping("/posts/{id}/images")
	public ResponseEntity<String> createPostGroupImages(@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestParam(value = "addedImages", required = false) List<MultipartFile> addedImages,
			@RequestParam(value = "deletedImageIds", required = false) List<String> deletedImageIds) {
		if (addedImages == null && deletedImageIds == null) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		Optional<PostGroupModel> optionalPostGroup = postGroupRepository.findById(id);
		if (optionalPostGroup.isEmpty()) {
			throw new AppException(71700, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}

		PostGroupModel postGroup = optionalPostGroup.get();

		List<PicturePostGroupModel> images = postGroup.getPictures();

		if (addedImages != null && deletedImageIds != null
				&& images.size() + addedImages.size() - deletedImageIds.size() > MAX_IMAGE_SIZE_PER_POST) {
			throw new AppException(71701, "Vượt quá giới hạn " + MAX_IMAGE_SIZE_PER_POST + " ảnh mỗi bài viết",
					HttpStatus.BAD_REQUEST);

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
							throw new AppException(71702, "Ảnh không tồn tại", HttpStatus.NOT_FOUND);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						throw new AppException(71703, "Lỗi xóa ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						e.printStackTrace();
						throw new AppException(71703, "Lỗi xóa ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
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
					String pictureUrl = imageUtils.saveImageToStorage(imageUtils.getPostGroupPath(postGroup.getGroupId(), id),
							addedImages.get(i),
							pictureId);
					postGroup.getPictures()
							.add(new PicturePostGroupModel(pictureId, postGroup, pictureUrl, order));
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(71704, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		postGroupRepository.save(postGroup);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("1 == @postGroupRepository.hasGroupMemberRoleByPostId(#id, #userId, \"CREATOR\") or "
			+ "1 == @postGroupRepository.hasGroupMemberRoleByPostId(#id, #userId, \"ADMIN\") or "
			+ "1 == @postGroupRepository.isGroupPostOwner(#id, #userId)")
    @DeleteMapping("/posts/{id}")
	public ResponseEntity<String> deletePostGroup(
			@RequestHeader("userId") String userId,
			@PathVariable String id) {
		// Find Group post
		Optional<PostGroupModel> optionalPostGroup = postGroupRepository.findById(id);
		if (optionalPostGroup.isEmpty()) {
			throw new AppException(71800, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}

		PostGroupModel postGroup = optionalPostGroup.get();

		List<PicturePostGroupModel> pictures = postGroup.getPictures();
		for (PicturePostGroupModel picture : pictures) {
			try {
				imageUtils.deleteImageFromStorageByUrl(picture.getPictureUrl());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new AppException(71801, "Ảnh không tồn tại", HttpStatus.NOT_FOUND);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(71802, "Lỗi xóa ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		pictures.clear();

		postGroup.setStatus(new StatusPostModel(4));
		postGroupRepository.save(postGroup);
		
		List<String> entityIds = commentPostGroupRepository.findByPostGroupId(id);
		entityIds.add(id);
		notificationService.deleteNotificationsByEntityIds(entityIds);
		
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
    
    @GetMapping("/{id}/comments")
    public ResponseEntity<HashMap<String, Object>> getPostComments(
			@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
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
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentPostGroupModel> parentComment = commentPostGroupRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			throw new AppException(72000, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
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
		if (comment.getContent() == null || comment.getContent().equals("")) {
			throw new AppException(72100, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}
		comment.setId(UUID.randomUUID().toString());
		comment.setPostGroup(new PostGroupModel(id));
		comment.setCreator(new UserModel(creator));
		
		try {
			commentPostGroupRepository.save(comment);
		} catch (JpaObjectRetrievalFailureException e) {
			throw new AppException(72101, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(72102, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}

		if (comment.getParentId() != null) {
			commentPostGroupRepository.commentCountIncrement(comment.getParentId(), 1);
			// Fetch the parent comment
			CommentPostGroupModel parentComment = commentPostGroupRepository.findById(comment.getParentId()).orElseThrow(() -> new AppException(72102, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND));
			
			if (!parentComment.getCreator().getId().equals(creator)) {
				// Create NotificationObject
				EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType("comment_post_group", NotificationType.CREATE)
				        .orElseGet(() -> entityTypeRepository.save(new EntityTypeModel(null, "comment_post_group", NotificationType.CREATE, null)));
				NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType, comment.getId(), new Date(), false);
				notificationObject = notificationObjectRepository.save(notificationObject);
				
				// Create NotificationChange
				NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject, new UserModel(creator), false);
				notificationChangeRepository.save(notificationChange);
				
				// Create Notification
				NotificationModel notification = new NotificationModel(null, notificationObject, parentComment.getCreator(), new StatusNotificationModel(1));
				notificationRepository.save(notification);
				
				Optional<UserModel> optionalUser = userRepository.findById(creator);
				PostGroupModel parentPost = postGroupRepository.findById(comment.getPostGroup().getId()).get();
				firebaseService.sendNotification(
						notification, notificationChange, notificationObject, 
						optionalUser.get().getAvatarUrl(), 
						optionalUser.get().getFullName() + " đã bình luận về bình luận của bạn",
						comment.getPostGroup().getId() + "," + parentPost.getGroupId());
			}
		} else {
			postGroupRepository.commentCountIncrement(id, 1);
			
			// Fetch the parent post
			PostGroupModel parentPost = postGroupRepository.findById(comment.getPostGroup().getId()).get();
			
			if (!parentPost.getCreator().getId().equals(creator)) {
				// Create NotificationObject
				EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType("comment_post_group", NotificationType.CREATE)
				        .orElseGet(() -> entityTypeRepository.save(new EntityTypeModel(null, "comment_post_group", NotificationType.CREATE, null)));
				NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType, comment.getId(), new Date(), false);
				notificationObject = notificationObjectRepository.save(notificationObject);
				
				// Create NotificationChange
				NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject, new UserModel(creator), false);
				notificationChangeRepository.save(notificationChange);
				
				// Create Notification
				NotificationModel notification = new NotificationModel(null, notificationObject, parentPost.getCreator(), new StatusNotificationModel(1));
				notificationRepository.save(notification);
				
				Optional<UserModel> optionalUser = userRepository.findById(creator);
				firebaseService.sendNotification(
						notification, notificationChange, notificationObject, 
						optionalUser.get().getAvatarUrl(), 
						optionalUser.get().getFullName() + " đã bình luận về bài viết của bạn",
						comment.getPostGroup().getId() + "," + parentPost.getGroupId());
			}
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("1 == @commentPostGroupRepository.isCommentOwner(#commentId, #userId)")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId, @RequestBody CommentPostGroupModel updatedComment) {
		if (updatedComment.getContent() == null || updatedComment.getContent().equals("")) {
			throw new AppException(72200, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
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
		if (optionalComment.isEmpty()) {
			throw new AppException(72300, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}
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
		
		// Delete notifications for the comment and its children
		List<String> allCommentIds = commentPostGroupRepository.findByParentIds(allParentId);
		allCommentIds.add(commentId);
		notificationService.deleteNotificationsByEntityIds(allCommentIds);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	@GetMapping("/{id}/react")
	public ResponseEntity<HashMap<String, Object>> getPostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestParam Integer reactId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString("desc"), "createAt"));
		Page<IInteractPostGroupDto> users = interactPostGroupRepository.getReactionUsers(id, reactId,
				pageable);

		result.put("totalPages", users.getTotalPages());
		result.put("users", users.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PostMapping("/{id}/react")
	public ResponseEntity<String> postPostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		Optional<InteractPostGroupModel> optionalInteractPostGroup = interactPostGroupRepository
				.findById(new InteractPostGroupId(id, creatorId));

		if (!optionalInteractPostGroup.isEmpty() && optionalInteractPostGroup.get().getIsDelete() == false) {
			throw new AppException(72500, "Đã thả cảm xúc bài viết này", HttpStatus.BAD_REQUEST);
		}

		InteractPostGroupModel interactPostGroup = new InteractPostGroupModel(id, creatorId,
				req.getReactId());
		try {
			interactPostGroupRepository.save(interactPostGroup);
		} catch (JpaObjectRetrievalFailureException e) {
			throw new AppException(72501, "postId hoặc reactId không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		postGroupRepository.reactionCountIncrement(id, 1);
		
		// Notification creation logic
	    PostGroupModel postGroup = postGroupRepository.findById(id).get();
	    
	    if (!creatorId.equals(postGroup.getCreator().getId())) {
	        EntityTypeModel entityType = entityTypeRepository.findByEntityTableAndNotificationType("interact_post_group", NotificationType.CREATE)
	                .orElseGet(() -> entityTypeRepository.save(new EntityTypeModel(null, "interact_post_group", NotificationType.CREATE, null)));
	        
	        Optional<NotificationObjectModel> optionalNotificationObject = notificationObjectRepository
	                .findByEntityTypeAndEntityId(entityType, postGroup.getId());
	        
	        if (optionalNotificationObject.isPresent()) {
	            NotificationObjectModel notificationObject = optionalNotificationObject.get();
	            notificationChangeRepository.updateActorIdByNotificationObject(notificationObject.getId(), creatorId);
	            notificationRepository.updateStatusByNotificationObject(notificationObject.getId(), 1);
	        } else {
	            NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType, postGroup.getId(), new Date(), false);
	            notificationObject = notificationObjectRepository.save(notificationObject);

	            NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject, new UserModel(creatorId), false);
	            notificationChangeRepository.save(notificationChange);

	            NotificationModel notification = new NotificationModel(null, notificationObject, postGroup.getCreator(), new StatusNotificationModel((byte) 1));
	            notificationRepository.save(notification);
	            
	            Optional<UserModel> optionalUser = userRepository.findById(creatorId);
	            firebaseService.sendNotification(
	                    notification, notificationChange, notificationObject,
	                    optionalUser.get().getAvatarUrl(), 
						optionalUser.get().getFullName() + " đã bày tỏ cảm xúc về bài viết của bạn",
						postGroup.getGroupId());
	        }
	    }
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PutMapping("/{id}/react")
	public ResponseEntity<String> putPostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		if (req.getReactId() == null) {
			throw new AppException(72600, "reactId không được để trống", HttpStatus.BAD_REQUEST);
		}
		
		Optional<InteractPostGroupModel> optionalInteractPostGroup = interactPostGroupRepository
				.findById(new InteractPostGroupId(id, creatorId));

		if (optionalInteractPostGroup.isEmpty()) {
			throw new AppException(72601, "Chưa thả cảm xúc bài viết này", HttpStatus.BAD_REQUEST);
		}

		InteractPostGroupModel interactPostGroup = optionalInteractPostGroup.get();
		if (interactPostGroup.getReact().getId() == req.getReactId()) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		interactPostGroup.setReact(new ReactModel(req.getReactId()));
		try {
			interactPostGroupRepository.save(interactPostGroup);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(72602, "reactId không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@DeleteMapping("/{id}/react")
	public ResponseEntity<String> deletePostGroupReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id) {
		Optional<InteractPostGroupModel> optionalInteractPostGroup = interactPostGroupRepository
				.findById(new InteractPostGroupId(id, creatorId));

		if (optionalInteractPostGroup.isEmpty()) {
			throw new AppException(72700, "Chưa thả cảm xúc bài viết này", HttpStatus.BAD_REQUEST);
		}

		InteractPostGroupModel interactPostGroup = optionalInteractPostGroup.get();
		interactPostGroup.setIsDelete(true);
		postGroupRepository.reactionCountIncrement(id, -1);
		
		List<String> entityIds = new ArrayList<String>();
		entityIds.add(id);
		notificationService.deleteNotificationsByEntityIds(entityIds);
		
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	@GetMapping("/{postId}/votes/{voteId}")
	public ResponseEntity<HashMap<String, Object>> getVoteUsers(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "postId") String postId,
			@PathVariable Integer voteId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize) {
		if (pageSize == 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString("desc"), "createAt"));
		Page<IUserVotePostGroupDto> users = userVotePostGroupRepository.getUsers(voteId, postId, pageable);

		result.put("totalPages", users.getTotalPages());
		result.put("users", users.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("1 == @postGroupRepository.isMemberByPostId(#postId, #userId)")
	@PostMapping("/{postId}/votes/{voteId}")
	public ResponseEntity<HashMap<String, Object>> postVote(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "postId") String postId,
			@PathVariable Integer voteId) {
		if (!postGroupRepository.isAllowMultipleVotes(postId)
				&& userVotePostGroupRepository.userVoteCountByPost(postId, userId) >= 1) {
			throw new AppException(72900, "Chỉ được bình chọn tối đa 1 lựa chọn", HttpStatus.BAD_REQUEST);
		}

		UserVotePostGroupModel userVote = new UserVotePostGroupModel(userId, voteId, postId);
		try {
			userVotePostGroupRepository.save(userVote);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(72901, "Lựa chọn không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		voteOptionPostGroupRepository.voteCountIncrement(voteId, postId, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("1 == @postGroupRepository.isMemberByPostId(#postId, #userId)")
	@PutMapping("/{postId}/votes/{voteId}")
	public ResponseEntity<HashMap<String, Object>> putVote(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "postId") String postId,
			@PathVariable(name = "voteId") Integer oldVoteId,
			@RequestBody HashMap<String, String> body) {
		Integer updatedVoteId = Integer.valueOf(body.get("updatedVoteId"));
		try {
			int updated = userVotePostGroupRepository.updateVoteOption(updatedVoteId, userId, oldVoteId, postId);
			if (updated == 0) {
				throw new AppException(73000, "Không tìm thấy bình chọn", HttpStatus.BAD_REQUEST);
			}
			voteOptionPostGroupRepository.voteCountIncrement(oldVoteId, postId, -1);
			voteOptionPostGroupRepository.voteCountIncrement(updatedVoteId, postId, 1);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(73001, "Lựa chọn cập nhật không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("1 == @postGroupRepository.isMemberByPostId(#postId, #userId)")
	@DeleteMapping("/{postId}/votes/{voteId}")
	public ResponseEntity<String> deleteVote(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "postId") String postId,
			@PathVariable Integer voteId) {
		userVotePostGroupRepository.deleteById(new UserVotePostGroupId(userId, voteId, postId));
		voteOptionPostGroupRepository.voteCountIncrement(voteId, postId, -1);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PostMapping("/{postId}/votes")
	public ResponseEntity<Map<String, Object>> addPostVoteOption(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "postId") String postId,
			@RequestBody VoteRequestDto reqVoteOption) {
		if (reqVoteOption.getName() == null || reqVoteOption.getName().isBlank()) {
			throw new AppException(73200, "Tên lựa chọn không được để trống", HttpStatus.BAD_REQUEST);
		}

		Integer maxVoteId = voteOptionPostGroupRepository.getMaxVoteId(postId);
		if (maxVoteId == null) {
			throw new AppException(73201, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		boolean isAllowAddOptions = postGroupRepository.isAllowAddOptions(postId);
		if (!isAllowAddOptions) {
			throw new AppException(73202, "Không thể thêm lựa chọn", HttpStatus.BAD_REQUEST);
		}
		if (maxVoteId >= MAXIMUM_VOTE_OPTIONS) {
			throw new AppException(73203, "Số lượng lựa chọn không được vượt quá " + MAXIMUM_VOTE_OPTIONS,
					HttpStatus.BAD_REQUEST);
		}

		VoteOptionPostGroupModel voteOption = new VoteOptionPostGroupModel(maxVoteId + 1, new PostGroupModel(postId),
				reqVoteOption.getName());
		var returnedVoteOption = voteOptionPostGroupRepository.save(voteOption);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Collections.singletonMap("vote", mapper.map(returnedVoteOption, PostGroupDto.Votes.class)));
	}
	
	// Get a specific comment of a post
	@GetMapping("/{postId}/comments/{commentId}")
	public ResponseEntity<Map<String, Object>> getSingleCommentOfAPost(
			@RequestHeader("userId") String userId,
			@PathVariable String postId,
			@PathVariable String commentId) {
		HashMap<String, Object> result = new HashMap<String, Object>();
	
		boolean canDelete = false;
		if (1 == commentPostGroupRepository.hasGroupMemberRoleByCommentId(commentId, userId, "CREATOR") || 
				1 == commentPostGroupRepository.hasGroupMemberRoleByCommentId(commentId, userId, "ADMIN")) {
			canDelete = true;
		}
	
		ICommentPostGroupDto comment = commentPostGroupRepository.getComment(postId, commentId, userId, canDelete)
				.orElse(null);
		if (comment == null) {
			throw new AppException(73300, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}
	
		result.put("comment", comment);
	
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
