package hcmus.alumni.news.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.transaction.annotation.Transactional;
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

import hcmus.alumni.news.utils.FirebaseService;
import hcmus.alumni.news.repository.notification.EntityTypeRepository;
import hcmus.alumni.news.repository.notification.NotificationChangeRepository;
import hcmus.alumni.news.repository.notification.NotificationObjectRepository;
import hcmus.alumni.news.repository.notification.NotificationRepository;
import hcmus.alumni.news.repository.UserRepository;
import hcmus.alumni.news.common.CommentNewsPermissions;
import hcmus.alumni.news.common.FetchNewsMode;
import hcmus.alumni.news.common.NotificationType;
import hcmus.alumni.news.model.notification.EntityTypeModel;
import hcmus.alumni.news.model.notification.NotificationChangeModel;
import hcmus.alumni.news.model.notification.NotificationModel;
import hcmus.alumni.news.model.notification.NotificationObjectModel;
import hcmus.alumni.news.model.notification.StatusNotificationModel;
import hcmus.alumni.news.dto.CommentNewsDto;
import hcmus.alumni.news.dto.ICommentNewsDto;
import hcmus.alumni.news.dto.INewsDto;
import hcmus.alumni.news.dto.INewsListDto;
import hcmus.alumni.news.exception.AppException;
import hcmus.alumni.news.model.CommentNewsModel;
import hcmus.alumni.news.model.FacultyModel;
import hcmus.alumni.news.model.NewsModel;
import hcmus.alumni.news.model.StatusPostModel;
import hcmus.alumni.news.model.TagModel;
import hcmus.alumni.news.model.UserModel;
import hcmus.alumni.news.repository.CommentNewsRepository;
import hcmus.alumni.news.repository.NewsRepository;
import hcmus.alumni.news.repository.TagRepository;
import hcmus.alumni.news.utils.ImageUtils;
import hcmus.alumni.news.utils.NotificationService;
import jakarta.persistence.EntityManager;

@RestController
@RequestMapping("/news")
public class NewsServiceController {
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private NewsRepository newsRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private CommentNewsRepository commentNewsRepository;
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
	private final static int ADMIN_ROLE_ID = 1;
	private final static int FACULTY_MANAGER_ROLE_ID = 2;

	@GetMapping("/count")
	public ResponseEntity<Long> getNewsCount(
			@RequestParam(value = "statusId", defaultValue = "0") Integer statusId) {
		if (statusId.equals(0)) {
			return ResponseEntity.status(HttpStatus.OK).body(newsRepository.getCountByNotDelete());
		}
		return ResponseEntity.status(HttpStatus.OK).body(newsRepository.getCountByStatusId(statusId));
	}

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getNews(
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "facultyId", required = false) Integer facultyId,
			@RequestParam(value = "tagNames", required = false) List<String> tagNames,
			@RequestParam(value = "statusId", required = false) Integer statusId,
			@RequestParam(value = "fetchMode", required = false, defaultValue = "NORMAL") FetchNewsMode fetchMode) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		if (title.isBlank()) {
			title = null;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (tagNames != null) {
			for (int i = 0; i < tagNames.size(); i++) {
				tagNames.set(i, TagModel.sanitizeTagName(tagNames.get(i)));
			}
		}

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<INewsListDto> news = null;

			if (fetchMode.equals(FetchNewsMode.MANAGEMENT)) {
				// Check if is Admin or FacultyManager
				Set<Integer> roleIds = userRepository.getRoleIds(userId);
				if (roleIds.contains(ADMIN_ROLE_ID)) {
					news = newsRepository.searchNews(title, null, tagNames, statusId, pageable);
				} else if (roleIds.contains(FACULTY_MANAGER_ROLE_ID)) {
					news = newsRepository.searchNewsByUserFaculty(userId, title, tagNames, statusId, pageable);
				}
			}

			if (news == null) {
				news = newsRepository.searchNews(title, facultyId, tagNames, statusId, pageable);
			}

			result.put("totalPages", news.getTotalPages());
			result.put("news", news.getContent());
		} catch (IllegalArgumentException e) {
			throw new AppException(40200, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(40201, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<INewsDto> getNewsById(@PathVariable String id) {
		Optional<INewsDto> optionalNews = newsRepository.findNewsById(id);
		if (optionalNews.isEmpty()) {
			throw new AppException(40300, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		newsRepository.viewsIncrement(id);
		return ResponseEntity.status(HttpStatus.OK).body(optionalNews.get());
	}

	@PreAuthorize("hasAnyAuthority('News.Create')")
	@PostMapping("")
	public ResponseEntity<String> createNews(@RequestHeader("userId") String creator,
			@RequestParam(value = "title") String title, @RequestParam(value = "thumbnail") MultipartFile thumbnail,
			@RequestParam(value = "summary", required = false, defaultValue = "") String summary,
			@RequestParam(value = "tagNames", required = false) List<String> tagNames,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
			@RequestParam(value = "scheduledTime", required = false) Long scheduledTimeMili) {
		if (title.isEmpty()) {
			throw new AppException(40400, "Tiêu đề không được để trống",
					HttpStatus.BAD_REQUEST);
		}
		if (thumbnail.isEmpty()) {
			throw new AppException(40401, "Ảnh thumbnail không được để trống",
					HttpStatus.BAD_REQUEST);
		}
		if (tagNames != null && tagNames.size() > MAXIMUM_TAGS) {
			throw new AppException(40403, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
		String id = UUID.randomUUID().toString();

		try {
			// Save thumbnail image
			String thumbnailUrl = imageUtils.saveImageToStorage(imageUtils.getNewsPath(id), thumbnail, "thumbnail");
			// Save news to database
			NewsModel news = new NewsModel(id, new UserModel(creator), title, summary, "", thumbnailUrl);
			// Lên lịch
			if (scheduledTimeMili != null) {
				news.setPublishedAt(new Date(scheduledTimeMili));
				news.setStatus(new StatusPostModel(1));
			} else {
				news.setPublishedAt(new Date());
			}
			if (tagNames != null) {
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
				news.setTags(tags);
			}
			if (!facultyId.equals(0)) {
				news.setFaculty(new FacultyModel(facultyId));
			}
			newsRepository.save(news);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(40402, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('News.Edit')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateNews(@PathVariable String id,
			@RequestParam(value = "title", defaultValue = "") String title,
			@RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
			@RequestParam(value = "summary", required = false, defaultValue = "") String summary,
			@RequestParam(value = "tagNames", required = false) List<String> tagNames,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
			@RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId) {
		boolean isPut = false;
		if (tagNames != null && tagNames.size() > MAXIMUM_TAGS) {
			throw new AppException(40502, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}

		try {
			// Find news
			Optional<NewsModel> optionalNews = newsRepository.findById(id);
			if (optionalNews.isEmpty()) {
				throw new AppException(40500, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
			}
			NewsModel news = optionalNews.get();
			if (thumbnail != null && !thumbnail.isEmpty()) {
				// Overwrite old thumbnail
				imageUtils.saveImageToStorage(imageUtils.getNewsPath(id), thumbnail, "thumbnail");
			}
			if (!title.equals("")) {
				news.setTitle(title);
				isPut = true;
			}
			if (!summary.equals("")) {
				news.setSummary(summary);
				isPut = true;
			}
			if (tagNames != null) {
				Set<TagModel> currentTags = news.getTags();
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
				isPut = true;
			}
			if (!facultyId.equals(0)) {
				news.setFaculty(new FacultyModel(facultyId));
				isPut = true;
			}
			if (!statusId.equals(0)) {
				news.setStatus(new StatusPostModel(statusId));
				isPut = true;
			}
			if (isPut) {
				newsRepository.save(news);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new AppException(40501, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('News.Delete')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteNews(@PathVariable String id) {
		// Find news
		Optional<NewsModel> optionalNews = newsRepository.findById(id);
		if (optionalNews.isEmpty()) {
			throw new AppException(40600, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		NewsModel news = optionalNews.get();
		news.setStatus(new StatusPostModel(4));
		newsRepository.save(news);

		List<String> commentIds = commentNewsRepository.findByNewsId(id);
		notificationService.deleteNotificationsByEntityIds(commentIds);

		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('News.Edit')")
	@PutMapping("/{id}/content")
	public ResponseEntity<String> updateNewsContent(@PathVariable String id,
			@RequestBody(required = false) NewsModel updatedNews) {
		if (updatedNews.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}
		// Find news
		Optional<NewsModel> optionalNews = newsRepository.findById(id);
		if (optionalNews.isEmpty()) {
			throw new AppException(40700, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		NewsModel news = optionalNews.get();
		if (!updatedNews.getContent().equals("")) {
			String newContent = imageUtils.updateImgFromHtmlToStorage(news.getContent(), updatedNews.getContent(), id);
			if (newContent.equals(news.getContent())) {
				return ResponseEntity.status(HttpStatus.OK).body("");
			}
			news.setContent(newContent);
			newsRepository.save(news);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@GetMapping("/{id}/related")
	public ResponseEntity<HashMap<String, Object>> getRelatedNews(
			@PathVariable String id,
			@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
		if (limit <= 0 || limit > 10) {
			limit = 10;
		}
		Optional<NewsModel> optionalNews = newsRepository.findById(id);
		if (optionalNews.isEmpty()) {
			throw new AppException(40800, "Không tìm thấy bài viết gốc", HttpStatus.NOT_FOUND);
		}
		FacultyModel faculty = optionalNews.get().getFaculty();
		Integer facultyId = null;
		if (faculty != null) {
			facultyId = optionalNews.get().getFaculty().getId();
		}
		List<Long> tagIds = optionalNews.get().getTags().stream().map(TagModel::getId).collect(Collectors.toList());

		Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
		Page<INewsListDto> news = newsRepository.getRelatedNews(id, facultyId, tagIds, pageable);

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("news", news.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/hot")
	public ResponseEntity<HashMap<String, Object>> getHotNews(
			@RequestParam(value = "limit", defaultValue = "5") Integer limit,
			@RequestParam(value = "weeks", defaultValue = "2") Integer weeks) {
		if (limit <= 0 || limit > 10) {
			limit = 10;
		}
		if (weeks < 0) {
			weeks = -weeks;
		} else if (weeks == 0) {
			weeks = 2;
		}

		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		cal.add(Calendar.WEEK_OF_YEAR, -weeks);
		Date startDate = cal.getTime();

		Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "views"));
		Page<INewsListDto> news = newsRepository.getHotNews(startDate, endDate, pageable);

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("news", news.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get comments of a news
	@GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getNewsComments(
			Authentication authentication,
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("News.Comment.Delete"))) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentNewsDto> comments = commentNewsRepository.getComments(id, userId, canDelete, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get children comments of a comment
	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getNewsChildrenComments(
			Authentication authentication,
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@PathVariable String commentId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentNewsModel> parentComment = commentNewsRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			throw new AppException(41100, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("News.Comment.Delete"))) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentNewsDto> comments = commentNewsRepository.getChildrenComment(commentId, userId, canDelete,
				pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('News.Comment.Create')")
	@PostMapping("/{id}/comments")
	@Transactional
	public ResponseEntity<HashMap<String, Object>> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentNewsModel comment) {
		if (comment.getContent() == null || comment.getContent().equals("")) {
			throw new AppException(41200, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}

		HashMap<String, Object> result = new HashMap<String, Object>();

		comment.setId(UUID.randomUUID().toString());
		comment.setNews(new NewsModel(id));
		comment.setCreator(new UserModel(creator));

		try {
			CommentNewsModel savedCmt = commentNewsRepository.saveAndFlush(comment);
			entityManager.refresh(savedCmt);
			savedCmt.setPermissions(new CommentNewsPermissions(true, true));
			result.put("comment", mapper.map(savedCmt, CommentNewsDto.class));
		} catch (JpaObjectRetrievalFailureException e) {
			throw new AppException(41201, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(41202, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}

		if (comment.getParentId() != null) {
			commentNewsRepository.commentCountIncrement(comment.getParentId(), 1);
			// Fetch the parent comment
			CommentNewsModel parentComment = commentNewsRepository.findById(comment.getParentId())
					.orElseThrow(() -> new AppException(41202, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND));

			if (!parentComment.getCreator().getId().equals(creator)) {
				// Create NotificationObject
				EntityTypeModel entityType = entityTypeRepository
						.findByEntityTableAndNotificationType("comment_news", NotificationType.CREATE)
						.orElseGet(() -> entityTypeRepository
								.save(new EntityTypeModel(null, "comment_news", NotificationType.CREATE, null)));
				NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType,
						comment.getId(), new Date(), false);
				notificationObject = notificationObjectRepository.save(notificationObject);

				// Create NotificationChange
				NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject,
						new UserModel(creator), false);
				notificationChangeRepository.save(notificationChange);

				// Create Notification
				NotificationModel notification = new NotificationModel(null, notificationObject,
						parentComment.getCreator(), new StatusNotificationModel(1));
				notificationRepository.save(notification);

				Optional<UserModel> optionalUser = userRepository.findById(creator);
				firebaseService.sendNotification(
						notification, notificationChange, notificationObject,
						optionalUser.get().getAvatarUrl(),
						optionalUser.get().getFullName() + " đã bình luận về bình luận của bạn",
						comment.getNews().getId());
			}
		} else {
			newsRepository.commentCountIncrement(id, 1);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@PreAuthorize("1 == @commentNewsRepository.isCommentOwner(#commentId, #userId)")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId, @RequestBody CommentNewsModel updatedComment) {
		if (updatedComment.getContent() == null || updatedComment.getContent().equals("")) {
			throw new AppException(41300, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}

		commentNewsRepository.updateComment(commentId, userId, updatedComment.getContent());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAuthority('News.Comment.Delete') or 1 == @commentNewsRepository.isCommentOwner(#commentId, #userId)")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentNewsModel> optionalComment = commentNewsRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			throw new AppException(41400, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}
		CommentNewsModel originalComment = optionalComment.get();

		// Initilize variables
		List<CommentNewsModel> childrenComments = new ArrayList<CommentNewsModel>();
		List<String> allParentId = new ArrayList<String>();

		// Get children comments
		String curCommentId = commentId;
		allParentId.add(curCommentId);
		childrenComments.addAll(commentNewsRepository.getChildrenComment(curCommentId));

		// Start the loop
		while (!childrenComments.isEmpty()) {
			CommentNewsModel curComment = childrenComments.get(0);
			curCommentId = curComment.getId();
			List<CommentNewsModel> temp = commentNewsRepository.getChildrenComment(curCommentId);
			if (!temp.isEmpty()) {
				allParentId.add(curCommentId);
				childrenComments.addAll(temp);
			}

			childrenComments.remove(0);
		}

		// Delete all comments and update comment count
		int deleted = commentNewsRepository.deleteComment(commentId);
		for (String parentId : allParentId) {
			commentNewsRepository.deleteChildrenComment(parentId);
		}
		if (deleted != 0) {
			if (originalComment.getParentId() != null) {
				commentNewsRepository.commentCountIncrement(originalComment.getParentId(), -1);
			} else {
				newsRepository.commentCountIncrement(originalComment.getNews().getId(), -1);
			}
		}

		// Delete notifications for the comment and its children
		List<String> allCommentIds = commentNewsRepository.findByParentIds(allParentId);
		allCommentIds.add(commentId);
		notificationService.deleteNotificationsByEntityIds(allCommentIds);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	// Get a specific comment of a post
	@GetMapping("/{newsId}/comments/{commentId}")
	public ResponseEntity<Map<String, Object>> getSingleCommentOfAPost(
			Authentication authentication,
			@RequestHeader(value = "userId", defaultValue = "") String userId,
			@PathVariable String newsId,
			@PathVariable String commentId) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication != null && authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("News.Comment.Delete"))) {
			canDelete = true;
		}

		ICommentNewsDto comment = commentNewsRepository.getComment(newsId, commentId, userId, canDelete)
				.orElse(null);
		if (comment == null) {
			throw new AppException(41500, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}

		result.put("comment", comment);

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}
