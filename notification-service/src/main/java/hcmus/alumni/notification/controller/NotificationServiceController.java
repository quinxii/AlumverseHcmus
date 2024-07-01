package hcmus.alumni.notification.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
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

import hcmus.alumni.notification.dto.IUserSubscriptionTokenDto;
import hcmus.alumni.notification.dto.NotificationDto;

import hcmus.alumni.notification.model.user.UserSubscriptionTokenModel;
import hcmus.alumni.notification.model.notification.NotificationModel;
import hcmus.alumni.notification.model.notification.StatusNotificationModel;
import hcmus.alumni.notification.model.group.GroupModel;
import hcmus.alumni.notification.model.group.GroupMemberModel;
import hcmus.alumni.notification.model.group.PostGroupModel;
import hcmus.alumni.notification.model.counsel.PostAdviseModel;
import hcmus.alumni.notification.model.event.CommentEventModel;
import hcmus.alumni.notification.model.news.CommentNewsModel;
import hcmus.alumni.notification.model.group.CommentPostGroupModel;
import hcmus.alumni.notification.model.counsel.CommentPostAdviseModel;

import hcmus.alumni.notification.repository.UserSubscriptionTokenRepository;
import hcmus.alumni.notification.repository.NotificationRepository;

import hcmus.alumni.notification.exception.AppException;
import hcmus.alumni.notification.common.NotificationType;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/notification")
public class NotificationServiceController {
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	
	@Autowired
    private UserSubscriptionTokenRepository userSubscriptionTokenRepository;
	@Autowired
	private NotificationRepository notificationRepository;

	private final static int MAXIMUM_PAGES = 50;
	
	@GetMapping("/subscription")
    public ResponseEntity<List<IUserSubscriptionTokenDto>> getUserSubscriptionToken(
    		@RequestHeader("userId") String userId) {
        List<IUserSubscriptionTokenDto> userSubscriptionTokens = 
        		userSubscriptionTokenRepository.getUserSubscriptionTokenByUserId(userId);
        return new ResponseEntity<>(userSubscriptionTokens, HttpStatus.OK);
    }
	
	@PostMapping("/subscription")
    public ResponseEntity<String> addUserSubscriptionToken(
    		@RequestHeader("userId") String userId, 
    		@RequestBody Map<String, Object> userSubscriptionToken) {
        UserSubscriptionTokenModel createdToken = new UserSubscriptionTokenModel(userId, (String) userSubscriptionToken.get("token"));
        userSubscriptionTokenRepository.save(createdToken);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

	@DeleteMapping("/subscription")
    public ResponseEntity<Void> deleteUserSubscriptionToken(
    		@RequestHeader("userId") String userId, 
    		@RequestBody Map<String, Object> userSubscriptionToken) {
    	userSubscriptionTokenRepository.deleteUserSubscriptionToken(userId, (String) userSubscriptionToken.get("token"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getNotifications(
			@RequestHeader("userId") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		int totalUnreadNotification = notificationRepository.getUnreadNotificationsCount(userId);
		
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<NotificationModel> notificationsPages = notificationRepository.getNotifications(userId, pageable);
		List<NotificationModel> notifications = notificationsPages.getContent();
		
		for (NotificationModel notification : notifications) {
			if (NotificationType.CREATE.equals(notification.getNotificationType())) {
				switch (notification.getEntityTable()) {
					case "request_friend":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						notification.setNotificationMessage(notification.getActor().getFullName() + " đã gửi một lời mời kết bạn");
						break;
					case "comment_event":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về bình luận của bạn");
						Optional<CommentEventModel> optionalEventComment = notificationRepository.findCommentEventById(notification.getEntityId());
						if (optionalEventComment.isPresent()) {
							notification.setParentId(optionalEventComment.get().getEventId());
						}
						break;
					case "comment_news":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về bình luận của bạn");
						Optional<CommentNewsModel> optionalNewsComment = notificationRepository.findCommentNewsById(notification.getEntityId());
						if (optionalNewsComment.isPresent()) {
							notification.setParentId(optionalNewsComment.get().getNewsId());
						}
						break;
					case "request_join_group":
						Optional<GroupModel> optionalGroup = notificationRepository.findGroupById(notification.getEntityId());
						int requestCount = notificationRepository.getRequestJoinCount(notification.getEntityId());
						if (optionalGroup.isPresent()) {
						    notification.setNotificationImageUrl(optionalGroup.get().getCoverUrl());
						    notification.setNotificationMessage(notification.getActor().getFullName() + 
						    		" đã yêu cầu tham gia nhóm " + optionalGroup.get().getName());
						}
						break;
					case "interact_post_group":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						Optional<PostGroupModel> optionalPostGroup = notificationRepository.findPostGroupById(notification.getEntityId());
						if (optionalPostGroup.isPresent()) {
							notification.setNotificationMessage(notification.getActor().getFullName() + 
									(optionalPostGroup.get().getReactionCount() <= 1 ? "" : (" và " + (optionalPostGroup.get().getReactionCount() - 1) + " người khác")) + 
									" đã bày tỏ cảm xúc về bài viết của bạn");
							notification.setParentId(optionalPostGroup.get().getGroupId());
						}
						break;
					case "comment_post_group":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						Optional<CommentPostGroupModel> optionalCommentPostGroup = notificationRepository.findCommentPostGroupById(notification.getEntityId());
						if (optionalCommentPostGroup.isPresent()) {
							notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về " + 
									((optionalCommentPostGroup.get().getParentId() == null) ? "bài viết" : "bình luận") + " của bạn");
							Optional<PostGroupModel> postGroup = notificationRepository.findPostGroupById(optionalCommentPostGroup.get().getPostGroupId());
							if (postGroup.isPresent())
								notification.setParentId(optionalCommentPostGroup.get().getPostGroupId() + "," +
										postGroup.get().getGroupId());
						}
						break;
					case "interact_post_advise":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						Optional<PostAdviseModel> optionalPostAdvise = notificationRepository.findPostAdviseById(notification.getEntityId());
						if (optionalPostAdvise.isPresent()) {
							notification.setNotificationMessage(notification.getActor().getFullName() + 
									(optionalPostAdvise.get().getReactionCount() <= 1 ? "" : (" và " + (optionalPostAdvise.get().getReactionCount() - 1) + " người khác")) + 
									" đã bày tỏ cảm xúc về bài viết của bạn");
						}
						break;
					case "comment_post_advise":
						notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
						Optional<CommentPostAdviseModel> optionalCommentPostAdvise = notificationRepository.findCommentPostAdviseById(notification.getEntityId());
						if (optionalCommentPostAdvise.isPresent()) {
							notification.setParentId(optionalCommentPostAdvise.get().getPostAdviseId());
							notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về " + 
									((optionalCommentPostAdvise.get().getParentId() == null) ? "bài viết" : "bình luận") + " của bạn");
						}
						break;
	            }
			} else if (NotificationType.UPDATE.equals(notification.getNotificationType())) {
				switch (notification.getEntityTable()) {
					case "group":
						Optional<GroupModel> optionalGroup = notificationRepository.findGroupById(notification.getEntityId());
						if (optionalGroup.isPresent()) {
							notification.setNotificationImageUrl(optionalGroup.get().getCoverUrl());
						    notification.setNotificationMessage("Nhóm " + optionalGroup.get().getName() + " mà bạn tham gia đã được cập nhật thông tin");
						}
						break;
					case "request_join_group":
						Optional<GroupModel> group = notificationRepository.findGroupById(notification.getEntityId());
						if (group.isPresent()) 
							notification.setNotificationImageUrl(group.get().getCoverUrl());
						Optional<GroupMemberModel> optionalGroupMember = 
								notificationRepository.findGroupMemberById(notification.getNotifier().getId(), notification.getEntityId());
						notification.setNotificationMessage("Yêu cầu tham gia nhóm " + group.get().getName() + " đã " + 
								(optionalGroupMember.isPresent() ? "được chấp thuận" : "bị từ chối"));
						break;
				}
			}
	    }
		
		result.put("totalPages", notificationsPages.getTotalPages());
		result.put("totalUnreadNotification", totalUnreadNotification);
		result.put("notifications", notifications.stream().map(n -> mapper.map(n, NotificationDto.class)).toList());
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<String> updateNotificationStatus(
	        @PathVariable Long id) {
	    Optional<NotificationModel> optionalNotification = notificationRepository.findById(id);
	    
	    if (!optionalNotification.isPresent()) {
	    	throw new AppException(100500, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);
	    }
	
	    NotificationModel notification = optionalNotification.get();
	    notification.setStatus(new StatusNotificationModel(2));
	    notificationRepository.save(notification);
	    
	    return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
