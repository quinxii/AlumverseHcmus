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

import hcmus.alumni.notification.dto.NotificationDto;

import hcmus.alumni.notification.model.notification.NotificationModel;
import hcmus.alumni.notification.model.notification.StatusNotificationModel;
import hcmus.alumni.notification.model.group.GroupModel;
import hcmus.alumni.notification.model.group.PostGroupModel;
import hcmus.alumni.notification.model.counsel.PostAdviseModel;
import hcmus.alumni.notification.model.event.CommentEventModel;
import hcmus.alumni.notification.model.news.CommentNewsModel;
import hcmus.alumni.notification.model.group.CommentPostGroupModel;
import hcmus.alumni.notification.model.counsel.CommentPostAdviseModel;

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
	private NotificationRepository notificationRepository;

	private final static int MAXIMUM_PAGES = 50;

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getNotifications(
			@RequestHeader("userId") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		int totalUnreadMessages = notificationRepository.getUnreadNotificationsCount(userId);
		
		Pageable pageable = PageRequest.of(page, pageSize);
		Page<NotificationModel> notificationsPages = notificationRepository.getNotifications(userId, pageable);
		List<NotificationModel> notifications = notificationsPages.getContent();

	    for (NotificationModel notification : notifications) {
			if ("request_friend".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
			    notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
			    notification.setNotificationMessage(notification.getActor().getFullName() + " đã gửi một lời mời kết bạn");
			}
			if ("comment_event".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
			    notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
			    notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về bình luận của bạn");
			    Optional<CommentEventModel> optionalComment = notificationRepository.findCommentEventById(notification.getEntityId());
			    if (optionalComment.isPresent()) {
			    	notification.setParentId(optionalComment.get().getEventId());
			    }
			}
			if ("comment_news".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
			    notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
			    notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về bình luận của bạn");
			    Optional<CommentNewsModel> optionalComment = notificationRepository.findCommentNewsById(notification.getEntityId());
			    if (optionalComment.isPresent()) {
			    	notification.setParentId(optionalComment.get().getNewsId());
			    }
			}
			if ("group".equals(notification.getEntityTable()) && (NotificationType.UPDATE).equals(notification.getNotificationType())) {
				Optional<GroupModel> optionalGroup = notificationRepository.findGroupById(notification.getEntityId());
			    if (optionalGroup.isPresent()) {
			    	notification.setNotificationImageUrl(optionalGroup.get().getCoverUrl());
			        notification.setNotificationMessage("Nhóm " + optionalGroup.get().getName() + " mà bạn tham gia đã được cập nhật thông tin");
			    }
			}
			if ("interact_post_group".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
				notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
				Optional<PostGroupModel> optionalPost = notificationRepository.findPostGroupById(notification.getEntityId());
			    if (optionalPost.isPresent()) {
			    	notification.setNotificationMessage(notification.getActor().getFullName() + " và " + 
			    			(optionalPost.get().getReactionCount() - 1) + " người khác đã bày tỏ cảm xúc về bài viết của bạn");
			    }
			}
			if ("comment_post_group".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
				notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
				Optional<CommentPostGroupModel> optionalComment = notificationRepository.findCommentPostGroupById(notification.getEntityId());
			    if (optionalComment.isPresent()) {
			    	notification.setParentId(optionalComment.get().getPostGroupId());
			    	notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về " + 
			    			((optionalComment.get().getParentId() == null) ? "bài viết" : "bình luận") + " của bạn");
			    }
			}
			if ("interact_post_advise".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
				notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
				Optional<PostAdviseModel> optionalPost = notificationRepository.findPostAdviseById(notification.getEntityId());
			    if (optionalPost.isPresent()) {
			    	notification.setNotificationMessage(notification.getActor().getFullName() + " và " + 
			    			(optionalPost.get().getReactionCount() - 1) + " người khác đã bày tỏ cảm xúc về bài viết của bạn");
			    }
			}
			if ("comment_post_advise".equals(notification.getEntityTable()) && (NotificationType.CREATE).equals(notification.getNotificationType())) {
				notification.setNotificationImageUrl(notification.getActor().getAvatarUrl());
				Optional<CommentPostAdviseModel> optionalComment = notificationRepository.findCommentPostAdviseById(notification.getEntityId());
			    if (optionalComment.isPresent()) {
			    	notification.setParentId(optionalComment.get().getPostAdviseId());
			    	notification.setNotificationMessage(notification.getActor().getFullName() + " đã bình luận về " + 
			    			((optionalComment.get().getParentId() == null) ? "bài viết" : "bình luận") + " của bạn");
			    }
			}
	    }

		result.put("totalUnreadMessages", totalUnreadMessages);
		result.put("notifications", notifications.stream().map(n -> mapper.map(n, NotificationDto.class)).toList());
		
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<String> updateNotificationStatus(
	        @PathVariable Long id) {
	    Optional<NotificationModel> optionalNotification = notificationRepository.findById(id);
	    
	    if (!optionalNotification.isPresent()) {
	    	throw new AppException(100200, "Không tìm thấy thông báo", HttpStatus.NOT_FOUND);
	    }
	
	    NotificationModel notification = optionalNotification.get();
	    notification.setStatus(new StatusNotificationModel(2));
	    notificationRepository.save(notification);
	    
	    return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
