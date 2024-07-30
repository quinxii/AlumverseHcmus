package hcmus.alumni.counsel.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

import hcmus.alumni.counsel.dto.request.PostAdviseRequestDto;
import hcmus.alumni.counsel.dto.request.ReactRequestDto;
import hcmus.alumni.counsel.dto.request.PostAdviseRequestDto.TagRequestDto;
import hcmus.alumni.counsel.dto.request.PostAdviseRequestDto.VoteRequestDto;
import hcmus.alumni.counsel.dto.response.CommentPostAdviseDto;
import hcmus.alumni.counsel.dto.response.ICommentPostAdviseDto;
import hcmus.alumni.counsel.dto.response.ICommentWithPostDto;
import hcmus.alumni.counsel.dto.response.IInteractPostAdviseDto;
import hcmus.alumni.counsel.dto.response.IUserVotePostAdviseDto;
import hcmus.alumni.counsel.dto.response.PostAdviseDto;
import hcmus.alumni.counsel.exception.AppException;
import hcmus.alumni.counsel.model.CommentPostAdviseModel;
import hcmus.alumni.counsel.model.InteractPostAdviseId;
import hcmus.alumni.counsel.model.InteractPostAdviseModel;
import hcmus.alumni.counsel.model.PicturePostAdviseModel;
import hcmus.alumni.counsel.model.PostAdviseModel;
import hcmus.alumni.counsel.model.ReactModel;
import hcmus.alumni.counsel.model.StatusPostModel;
import hcmus.alumni.counsel.model.TagModel;
import hcmus.alumni.counsel.model.UserModel;
import hcmus.alumni.counsel.model.UserVotePostAdviseId;
import hcmus.alumni.counsel.model.UserVotePostAdviseModel;
import hcmus.alumni.counsel.model.VoteOptionPostAdviseModel;
import hcmus.alumni.counsel.model.notification.EntityTypeModel;
import hcmus.alumni.counsel.model.notification.NotificationChangeModel;
import hcmus.alumni.counsel.model.notification.NotificationModel;
import hcmus.alumni.counsel.model.notification.NotificationObjectModel;
import hcmus.alumni.counsel.model.notification.StatusNotificationModel;
import hcmus.alumni.counsel.repository.CommentPostAdviseRepository;
import hcmus.alumni.counsel.repository.InteractPostAdviseRepository;
import hcmus.alumni.counsel.repository.PostAdviseRepository;
import hcmus.alumni.counsel.repository.TagRepository;
import hcmus.alumni.counsel.repository.UserVotePostAdviseRepository;
import hcmus.alumni.counsel.repository.VoteOptionPostAdviseRepository;
import hcmus.alumni.counsel.repository.notification.EntityTypeRepository;
import hcmus.alumni.counsel.repository.notification.NotificationChangeRepository;
import hcmus.alumni.counsel.repository.notification.NotificationObjectRepository;
import hcmus.alumni.counsel.repository.notification.NotificationRepository;
import hcmus.alumni.counsel.utils.ImageUtils;
import hcmus.alumni.counsel.repository.UserRepository;
import hcmus.alumni.counsel.utils.FirebaseService;
import hcmus.alumni.counsel.utils.NotificationService;
import jakarta.persistence.EntityManager;
import hcmus.alumni.counsel.common.CommentPostAdvisePermissions;
import hcmus.alumni.counsel.common.NotificationType;

@RestController
@RequestMapping("/counsel")
public class CounselServiceController {
	@Autowired
	private final ModelMapper mapper = new ModelMapper();
	@Autowired
	private EntityManager entityManager;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PostAdviseRepository postAdviseRepository;
	@Autowired
	private CommentPostAdviseRepository commentPostAdviseRepository;
	@Autowired
	private InteractPostAdviseRepository interactPostAdviseRepository;
	@Autowired
	private VoteOptionPostAdviseRepository voteOptionPostAdviseRepository;
	@Autowired
	private UserVotePostAdviseRepository userVotePostAdviseRepository;
	@Autowired
	private TagRepository tagRepository;
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
	public ResponseEntity<HashMap<String, Object>> getPosts(
			Authentication authentication,
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
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Counsel.Delete"))) {
			canDelete = true;
		}

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<PostAdviseModel> postsPage = postAdviseRepository.searchPostAdvise(title, userId, canDelete, tagNames,
					pageable);

			result.put("totalPages", postsPage.getTotalPages());
			List<PostAdviseModel> postList = postsPage.getContent();
			List<Object[]> resultList = userVotePostAdviseRepository.getVoteIdsByUserAndPosts(userId,
					postList.stream().map(PostAdviseModel::getId).toList());

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
			for (PostAdviseModel post : postList) {
				Set<Integer> voteIds = voteIdsMap.get(post.getId());
				if (voteIds == null) {
					continue;
				}
				for (VoteOptionPostAdviseModel vote : post.getVotes()) {
					if (voteIds.contains(vote.getId().getVoteId())) {
						vote.setIsVoted(true);
					}
				}
			}

			result.put("posts", postList.stream().map(p -> mapper.map(p, PostAdviseDto.class)).toList());
		} catch (IllegalArgumentException e) {
			throw new AppException(60100, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(60101, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<PostAdviseDto> getPostById(Authentication authentication,
			@RequestHeader("userId") String userId, @PathVariable String id) {
		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Counsel.Delete"))) {
			canDelete = true;
		}

		PostAdviseModel post = postAdviseRepository.findPostAdviseById(id, userId, canDelete).orElse(null);

		if (post == null) {
			throw new AppException(60200, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}

		Set<Integer> voteIds = userVotePostAdviseRepository.getVoteIdsByUserAndPost(userId, id);

		for (VoteOptionPostAdviseModel vote : post.getVotes()) {
			if (voteIds.contains(vote.getId().getVoteId())) {
				vote.setIsVoted(true);
			}
		}

		return ResponseEntity.status(HttpStatus.OK).body(mapper.map(post, PostAdviseDto.class));
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Create')")
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> createPostAdvise(@RequestHeader("userId") String creator,
			@RequestBody PostAdviseRequestDto reqPostAdvise) {
		if (reqPostAdvise.getTitle() == null || reqPostAdvise.getTitle().isBlank()) {
			throw new AppException(60300, "Tiêu đề không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (reqPostAdvise.getContent() == null || reqPostAdvise.getContent().isBlank()) {
			throw new AppException(60301, "Nội dung không được để trống", HttpStatus.BAD_REQUEST);
		}
		if (reqPostAdvise.getTags() != null && reqPostAdvise.getTags().size() > MAXIMUM_TAGS) {
			throw new AppException(60302, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
		if (reqPostAdvise.getVotes() != null && reqPostAdvise.getVotes().size() > MAXIMUM_VOTE_OPTIONS) {
			throw new AppException(60303, "Số lượng lựa chọn không được vượt quá " + MAXIMUM_VOTE_OPTIONS,
					HttpStatus.BAD_REQUEST);
		}

		// Handle tags
		Set<TagModel> tags = new HashSet<TagModel>();
		if (reqPostAdvise.getTags() != null) {
			for (TagRequestDto reqTag : reqPostAdvise.getTags()) {
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

		PostAdviseModel postAdvise = new PostAdviseModel(creator, reqPostAdvise);
		postAdvise.setTags(tags);
		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("id", postAdvise.getId()));
	}

	@PreAuthorize("1 == @postAdviseRepository.isPostOwner(#id, #userId)")
	@PutMapping("/{id}")
	public ResponseEntity<String> updatePost(
			@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestBody PostAdviseRequestDto updatedPostAdvise) {
		if (updatedPostAdvise.getTags() != null && updatedPostAdvise.getTags().size() > MAXIMUM_TAGS) {
			throw new AppException(60401, "Số lượng thẻ không được vượt quá " + MAXIMUM_TAGS, HttpStatus.BAD_REQUEST);
		}
		PostAdviseModel postAdvise = postAdviseRepository.findById(id).orElse(null);
		if (postAdvise == null) {
			throw new AppException(60400, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		if (postAdvise.getVotes().size() > 0) {
			throw new AppException(60402, "Không thể cập nhật bài viết đã có lựa chọn bình chọn",
					HttpStatus.BAD_REQUEST);
		}

		if (updatedPostAdvise.getTitle() != null && !updatedPostAdvise.getTitle().isBlank()) {
			postAdvise.setTitle(updatedPostAdvise.getTitle());
		}
		if (updatedPostAdvise.getContent() != null && !updatedPostAdvise.getContent().isBlank()) {
			postAdvise.setContent(updatedPostAdvise.getContent());
		}
		if (updatedPostAdvise.getTags() != null) {
			Set<TagModel> currentTags = postAdvise.getTags();
			Set<TagModel> updatedTags = new HashSet<TagModel>();

			for (var updatedTag : updatedPostAdvise.getTags()) {
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
		}

		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("1 == @postAdviseRepository.isPostOwner(#id, #userId)")
	@PutMapping("/{id}/images")
	public ResponseEntity<String> createPostAdviseImages(@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestParam(value = "addedImages", required = false) List<MultipartFile> addedImages,
			@RequestParam(value = "deletedImageIds", required = false) List<String> deletedImageIds) {
		if (addedImages == null && deletedImageIds == null) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		Optional<PostAdviseModel> optionalPostAdvise = postAdviseRepository.findById(id);
		if (optionalPostAdvise.isEmpty()) {
			throw new AppException(60500, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		PostAdviseModel postAdvise = optionalPostAdvise.get();

		List<PicturePostAdviseModel> images = postAdvise.getPictures();

		if (addedImages != null && deletedImageIds != null
				&& images.size() + addedImages.size() - deletedImageIds.size() > MAX_IMAGE_SIZE_PER_POST) {
			throw new AppException(60501, "Vượt quá giới hạn " + MAX_IMAGE_SIZE_PER_POST + " ảnh mỗi bài viết",
					HttpStatus.BAD_REQUEST);

		}

		// Delete images
		if (deletedImageIds != null && deletedImageIds.size() != 0) {
			List<PicturePostAdviseModel> deletedImages = new ArrayList<PicturePostAdviseModel>();
			for (PicturePostAdviseModel image : images) {
				if (deletedImageIds.contains(image.getId())) {
					try {
						boolean successful = imageUtils.deleteImageFromStorageByUrl(image.getPictureUrl());
						if (successful) {
							deletedImages.add(image);
						} else {
							throw new AppException(60502, "Ảnh không tồn tại", HttpStatus.NOT_FOUND);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						throw new AppException(60503, "Lỗi xóa ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (IOException e) {
						e.printStackTrace();
						throw new AppException(60503, "Lỗi xóa ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
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
					String pictureUrl = imageUtils.saveImageToStorage(imageUtils.getCounselPath(id),
							addedImages.get(i),
							pictureId);
					postAdvise.getPictures()
							.add(new PicturePostAdviseModel(pictureId, postAdvise, pictureUrl, order));
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(60504, "Lỗi lưu ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Delete') or 1 == @postAdviseRepository.isPostOwner(#id, #userId)")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deletePost(
			@RequestHeader("userId") String userId,
			@PathVariable String id) {
		// Find advise post
		Optional<PostAdviseModel> optionalPostAdvise = postAdviseRepository.findById(id);
		if (optionalPostAdvise.isEmpty()) {
			throw new AppException(60600, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}

		PostAdviseModel postAdvise = optionalPostAdvise.get();

		List<PicturePostAdviseModel> pictures = postAdvise.getPictures();
		for (PicturePostAdviseModel picture : pictures) {
			try {
				imageUtils.deleteImageFromStorageByUrl(picture.getPictureUrl());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new AppException(60601, "Ảnh không tồn tại", HttpStatus.NOT_FOUND);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AppException(60602, "Lỗi xóa ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		pictures.clear();

		postAdvise.setStatus(new StatusPostModel(4));
		postAdviseRepository.save(postAdvise);

		List<String> entityIds = commentPostAdviseRepository.findByPostAdviseId(id);
		entityIds.add(id);
		notificationService.deleteNotificationsByEntityIds(entityIds);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	// Get comments of a post
	@GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getPostComments(
			Authentication authentication,
			@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Counsel.Comment.Delete"))) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostAdviseDto> comments = commentPostAdviseRepository.getComments(id, userId, canDelete, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get children comments of a comment
	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getPostChildrenComments(
			Authentication authentication,
			@RequestHeader("userId") String userId,
			@PathVariable String commentId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentPostAdviseModel> parentComment = commentPostAdviseRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			throw new AppException(60800, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Counsel.Comment.Delete"))) {
			canDelete = true;
		}

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostAdviseDto> comments = commentPostAdviseRepository.getChildrenComment(commentId, userId,
				canDelete,
				pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Comment.Create')")
	@PostMapping("/{id}/comments")
	@Transactional
	public ResponseEntity<HashMap<String, Object>> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentPostAdviseModel comment) {
		if (comment.getContent() == null || comment.getContent().equals("")) {
			throw new AppException(60900, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}

		HashMap<String, Object> result = new HashMap<String, Object>();

		comment.setId(UUID.randomUUID().toString());
		comment.setPostAdvise(new PostAdviseModel(id));
		comment.setCreator(new UserModel(creator));

		try {
			CommentPostAdviseModel savedCmt = commentPostAdviseRepository.saveAndFlush(comment);
			entityManager.refresh(savedCmt);
			savedCmt.setPermissions(new CommentPostAdvisePermissions(true, true));
			result.put("comment", mapper.map(savedCmt, CommentPostAdviseDto.class));
		} catch (JpaObjectRetrievalFailureException e) {
			throw new AppException(40901, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(40902, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND);
		}

		if (comment.getParentId() != null) {
			commentPostAdviseRepository.commentCountIncrement(comment.getParentId(), 1);

			// Fetch the parent comment
			CommentPostAdviseModel parentComment = commentPostAdviseRepository.findById(comment.getParentId())
					.orElseThrow(() -> new AppException(40902, "Không tìm thấy bình luận cha", HttpStatus.NOT_FOUND));

			if (!parentComment.getCreator().getId().equals(creator)) {
				// Create NotificationObject
				EntityTypeModel entityType = entityTypeRepository
						.findByEntityTableAndNotificationType("comment_post_advise", NotificationType.CREATE)
						.orElseGet(() -> entityTypeRepository
								.save(new EntityTypeModel(null, "comment_post_advise", NotificationType.CREATE, null)));
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
						comment.getPostAdvise().getId());
			}
		} else {
			postAdviseRepository.commentCountIncrement(id, 1);

			// Fetch the parent post
			PostAdviseModel parentPost = postAdviseRepository.findById(comment.getPostAdvise().getId()).get();

			if (!parentPost.getCreator().getId().equals(creator)) {
				// Create NotificationObject
				EntityTypeModel entityType = entityTypeRepository
						.findByEntityTableAndNotificationType("comment_post_advise", NotificationType.CREATE)
						.orElseGet(() -> entityTypeRepository
								.save(new EntityTypeModel(null, "comment_post_advise", NotificationType.CREATE, null)));
				NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType,
						comment.getId(), new Date(), false);
				notificationObject = notificationObjectRepository.save(notificationObject);

				// Create NotificationChange
				NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject,
						new UserModel(creator), false);
				notificationChangeRepository.save(notificationChange);

				// Create Notification
				NotificationModel notification = new NotificationModel(null, notificationObject,
						parentPost.getCreator(), new StatusNotificationModel(1));
				notificationRepository.save(notification);

				Optional<UserModel> optionalUser = userRepository.findById(creator);
				firebaseService.sendNotification(
						notification, notificationChange, notificationObject,
						optionalUser.get().getAvatarUrl(),
						optionalUser.get().getFullName() + " đã bình luận về bài viết của bạn",
						comment.getPostAdvise().getId());
			}
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@PreAuthorize("1 == @commentPostAdviseRepository.isCommentOwner(#commentId, #userId)")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId, @RequestBody CommentPostAdviseModel updatedComment) {
		if (updatedComment.getContent() == null || updatedComment.getContent().equals("")) {
			throw new AppException(61000, "Nội dung bình luận không được để trống", HttpStatus.BAD_REQUEST);
		}

		commentPostAdviseRepository.updateComment(commentId, userId, updatedComment.getContent());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Comment.Delete') or 1 == @commentPostAdviseRepository.isCommentOwner(#commentId, #userId)")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentPostAdviseModel> optionalComment = commentPostAdviseRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			throw new AppException(61100, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}
		CommentPostAdviseModel originalComment = optionalComment.get();

		// Initilize variables
		List<CommentPostAdviseModel> childrenComments = new ArrayList<CommentPostAdviseModel>();
		List<String> allParentId = new ArrayList<String>();

		// Get children comments
		String curCommentId = commentId;
		allParentId.add(curCommentId);
		childrenComments.addAll(commentPostAdviseRepository.getChildrenComment(curCommentId));

		// Start the loop
		while (!childrenComments.isEmpty()) {
			CommentPostAdviseModel curComment = childrenComments.get(0);
			curCommentId = curComment.getId();
			List<CommentPostAdviseModel> temp = commentPostAdviseRepository.getChildrenComment(curCommentId);
			if (!temp.isEmpty()) {
				allParentId.add(curCommentId);
				childrenComments.addAll(temp);
			}

			childrenComments.remove(0);
		}

		// Delete all comments and update comment count
		int deleted = commentPostAdviseRepository.deleteComment(commentId);
		for (String parentId : allParentId) {
			commentPostAdviseRepository.deleteChildrenComment(parentId);
		}
		if (deleted != 0) {
			if (originalComment.getParentId() != null) {
				commentPostAdviseRepository.commentCountIncrement(originalComment.getParentId(), -1);
			} else {
				postAdviseRepository.commentCountIncrement(originalComment.getPostAdvise().getId(), -1);
			}
		}

		// Delete notifications for the comment and its children
		List<String> allCommentIds = commentPostAdviseRepository.findByParentIds(allParentId);
		allCommentIds.add(commentId);
		notificationService.deleteNotificationsByEntityIds(allCommentIds);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@GetMapping("/{id}/react")
	public ResponseEntity<HashMap<String, Object>> getPostAdviseReactionUsers(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestParam Integer reactId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString("desc"), "createAt"));
		Page<IInteractPostAdviseDto> users = interactPostAdviseRepository.getReactionUsers(id, reactId,
				pageable);

		result.put("totalPages", users.getTotalPages());
		result.put("users", users.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Reaction.Create')")
	@PostMapping("/{id}/react")
	public ResponseEntity<String> postPostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		Optional<InteractPostAdviseModel> optionalInteractPostAdvise = interactPostAdviseRepository
				.findById(new InteractPostAdviseId(id, creatorId));

		if (!optionalInteractPostAdvise.isEmpty() && optionalInteractPostAdvise.get().getIsDelete() == false) {
			throw new AppException(61300, "Đã thả cảm xúc bài viết này", HttpStatus.BAD_REQUEST);
		}

		InteractPostAdviseModel interactPostAdvise = new InteractPostAdviseModel(id, creatorId,
				req.getReactId());
		try {
			interactPostAdviseRepository.save(interactPostAdvise);
		} catch (JpaObjectRetrievalFailureException e) {
			throw new AppException(61301, "postId hoặc reactId không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		postAdviseRepository.reactionCountIncrement(id, 1);

		// Notification creation logic
		PostAdviseModel postAdvise = postAdviseRepository.findById(id).get();

		if (!creatorId.equals(postAdvise.getCreator().getId())) {
			EntityTypeModel entityType = entityTypeRepository
					.findByEntityTableAndNotificationType("interact_post_advise", NotificationType.CREATE)
					.orElseGet(() -> entityTypeRepository
							.save(new EntityTypeModel(null, "interact_post_advise", NotificationType.CREATE, null)));

			Optional<NotificationObjectModel> optionalNotificationObject = notificationObjectRepository
					.findByEntityTypeAndEntityId(entityType, postAdvise.getId());

			if (optionalNotificationObject.isPresent()) {
				NotificationObjectModel notificationObject = optionalNotificationObject.get();
				notificationChangeRepository.updateActorIdByNotificationObject(notificationObject.getId(), creatorId);
				notificationRepository.updateStatusByNotificationObject(notificationObject.getId(), 1);
			} else {
				NotificationObjectModel notificationObject = new NotificationObjectModel(null, entityType,
						postAdvise.getId(), new Date(), false);
				notificationObject = notificationObjectRepository.save(notificationObject);

				NotificationChangeModel notificationChange = new NotificationChangeModel(null, notificationObject,
						new UserModel(creatorId), false);
				notificationChangeRepository.save(notificationChange);

				NotificationModel notification = new NotificationModel(null, notificationObject,
						postAdvise.getCreator(), new StatusNotificationModel((byte) 1));
				notificationRepository.save(notification);

				Optional<UserModel> optionalUser = userRepository.findById(creatorId);
				firebaseService.sendNotification(
						notification, notificationChange, notificationObject,
						optionalUser.get().getAvatarUrl(),
						optionalUser.get().getFullName() + " đã bày tỏ cảm xúc về bài viết của bạn",
						null);
			}
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PutMapping("/{id}/react")
	public ResponseEntity<String> putPostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		if (req.getReactId() == null) {
			throw new AppException(61400, "reactId không được để trống", HttpStatus.BAD_REQUEST);
		}

		Optional<InteractPostAdviseModel> optionalInteractPostAdvise = interactPostAdviseRepository
				.findById(new InteractPostAdviseId(id, creatorId));

		if (optionalInteractPostAdvise.isEmpty()) {
			throw new AppException(61401, "Chưa thả cảm xúc bài viết này", HttpStatus.BAD_REQUEST);
		}

		InteractPostAdviseModel interactPostAdvise = optionalInteractPostAdvise.get();
		if (interactPostAdvise.getReact().getId() == req.getReactId()) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		interactPostAdvise.setReact(new ReactModel(req.getReactId()));
		try {
			interactPostAdviseRepository.save(interactPostAdvise);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(61402, "reactId không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@DeleteMapping("/{id}/react")
	public ResponseEntity<String> deletePostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id) {
		Optional<InteractPostAdviseModel> optionalInteractPostAdvise = interactPostAdviseRepository
				.findById(new InteractPostAdviseId(id, creatorId));

		if (optionalInteractPostAdvise.isEmpty()) {
			throw new AppException(61500, "Chưa thả cảm xúc bài viết này", HttpStatus.BAD_REQUEST);
		}

		InteractPostAdviseModel interactPostAdvise = optionalInteractPostAdvise.get();
		interactPostAdvise.setIsDelete(true);
		postAdviseRepository.reactionCountIncrement(id, -1);

		List<String> entityIds = new ArrayList<String>();
		entityIds.add(id);
		notificationService.deleteNotificationsByEntityIds(entityIds);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@GetMapping("/{id}/votes/{voteId}")
	public ResponseEntity<HashMap<String, Object>> getVoteUsers(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "id") String postId,
			@PathVariable Integer voteId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "50") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString("desc"), "createAt"));
		Page<IUserVotePostAdviseDto> users = userVotePostAdviseRepository.getUsers(voteId, postId, pageable);

		result.put("totalPages", users.getTotalPages());
		result.put("users", users.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Vote')")
	@PostMapping("/{id}/votes/{voteId}")
	public ResponseEntity<HashMap<String, Object>> postVote(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "id") String postId,
			@PathVariable Integer voteId) {
		if (!postAdviseRepository.isAllowMultipleVotes(postId)
				&& userVotePostAdviseRepository.userVoteCountByPost(postId, userId) >= 1) {
			throw new AppException(61700, "Chỉ được bình chọn tối đa 1 lựa chọn", HttpStatus.BAD_REQUEST);
		}

		UserVotePostAdviseModel userVote = new UserVotePostAdviseModel(userId, voteId, postId);
		try {
			userVotePostAdviseRepository.save(userVote);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(61701, "Lựa chọn không hợp lệ", HttpStatus.BAD_REQUEST);
		}
		voteOptionPostAdviseRepository.voteCountIncrement(voteId, postId, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Vote')")
	@PutMapping("/{id}/votes/{voteId}")
	public ResponseEntity<HashMap<String, Object>> putVote(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "id") String postId,
			@PathVariable(name = "voteId") Integer oldVoteId,
			@RequestBody HashMap<String, String> body) {
		Integer updatedVoteId = Integer.valueOf(body.get("updatedVoteId"));
		try {
			int updated = userVotePostAdviseRepository.updateVoteOption(updatedVoteId, userId, oldVoteId, postId);
			if (updated == 0) {
				throw new AppException(61800, "Không tìm thấy bình chọn", HttpStatus.BAD_REQUEST);
			}
			voteOptionPostAdviseRepository.voteCountIncrement(oldVoteId, postId, -1);
			voteOptionPostAdviseRepository.voteCountIncrement(updatedVoteId, postId, 1);
		} catch (DataIntegrityViolationException e) {
			throw new AppException(61801, "Lựa chọn cập nhật không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Vote')")
	@DeleteMapping("/{id}/votes/{voteId}")
	public ResponseEntity<String> deleteVote(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "id") String postId,
			@PathVariable Integer voteId) {
		userVotePostAdviseRepository.deleteById(new UserVotePostAdviseId(userId, voteId, postId));
		voteOptionPostAdviseRepository.voteCountIncrement(voteId, postId, -1);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Counsel.Vote')")
	@PostMapping("/{id}/votes")
	public ResponseEntity<Map<String, Object>> addPostVoteOption(
			@RequestHeader("userId") String userId,
			@PathVariable(name = "id") String postId,
			@RequestBody VoteRequestDto reqVoteOption) {
		if (reqVoteOption.getName() == null || reqVoteOption.getName().isBlank()) {
			throw new AppException(62000, "Tên lựa chọn không được để trống", HttpStatus.BAD_REQUEST);
		}

		Integer maxVoteId = voteOptionPostAdviseRepository.getMaxVoteId(postId);
		if (maxVoteId == null) {
			throw new AppException(62001, "Không tìm thấy bài viết", HttpStatus.NOT_FOUND);
		}
		boolean isAllowAddOptions = postAdviseRepository.isAllowAddOptions(postId);
		if (!isAllowAddOptions) {
			throw new AppException(62002, "Không thể thêm lựa chọn", HttpStatus.BAD_REQUEST);
		}
		if (maxVoteId >= MAXIMUM_VOTE_OPTIONS) {
			throw new AppException(62003, "Số lượng lựa chọn không được vượt quá " + MAXIMUM_VOTE_OPTIONS,
					HttpStatus.BAD_REQUEST);
		}

		VoteOptionPostAdviseModel voteOption = new VoteOptionPostAdviseModel(maxVoteId + 1, new PostAdviseModel(postId),
				reqVoteOption.getName());
		var returnedVoteOption = voteOptionPostAdviseRepository.save(voteOption);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Collections.singletonMap("vote", mapper.map(returnedVoteOption, PostAdviseDto.Votes.class)));
	}

	@PreAuthorize("#reqUserId.equals(#userId)")
	@GetMapping("/users/{userId}")
	public ResponseEntity<HashMap<String, Object>> getPostsOfUser(
			Authentication authentication,
			@RequestHeader("userId") String reqUserId,
			@PathVariable String userId,
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
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Counsel.Delete"))) {
			canDelete = true;
		}

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<PostAdviseModel> postsPage = postAdviseRepository.searchPostAdviseOfUser(userId, title, reqUserId,
					canDelete,
					tagNames, pageable);

			result.put("totalPages", postsPage.getTotalPages());
			List<PostAdviseModel> postList = postsPage.getContent();
			List<Object[]> resultList = userVotePostAdviseRepository.getVoteIdsByUserAndPosts(reqUserId,
					postList.stream().map(PostAdviseModel::getId).toList());

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
			for (PostAdviseModel post : postList) {
				Set<Integer> voteIds = voteIdsMap.get(post.getId());
				if (voteIds == null) {
					continue;
				}
				for (VoteOptionPostAdviseModel vote : post.getVotes()) {
					if (voteIds.contains(vote.getId().getVoteId())) {
						vote.setIsVoted(true);
					}
				}
			}

			result.put("posts", postList.stream().map(p -> mapper.map(p, PostAdviseDto.class)).toList());
		} catch (IllegalArgumentException e) {
			throw new AppException(62100, "Tham số order phải là 'asc' hoặc 'desc'", HttpStatus.BAD_REQUEST);
		} catch (InvalidDataAccessApiUsageException e) {
			throw new AppException(62101, "Tham số orderBy không hợp lệ", HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("#reqUserId.equals(#userId)")
	@GetMapping("/users/{userId}/comments")
	public ResponseEntity<HashMap<String, Object>> getCommentsOfUser(
			@RequestHeader("userId") String reqUserId,
			@PathVariable String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize <= 0 || pageSize > MAXIMUM_PAGES) {
			pageSize = MAXIMUM_PAGES;
		}

		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentWithPostDto> comments = commentPostAdviseRepository.getCommentsByUserId(userId, true, pageable);

		result.put("totalPages", comments.getTotalPages());
		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get a specific comment of a post
	@GetMapping("/{postId}/comments/{commentId}")
	public ResponseEntity<Map<String, Object>> getSingleCommentOfAPost(
			Authentication authentication,
			@RequestHeader("userId") String userId,
			@PathVariable String postId,
			@PathVariable String commentId) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Delete all post permissions regardless of being creator or not
		boolean canDelete = false;
		if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("Counsel.Comment.Delete"))) {
			canDelete = true;
		}

		ICommentPostAdviseDto comment = commentPostAdviseRepository.getComment(postId, commentId, userId, canDelete)
				.orElse(null);
		if (comment == null) {
			throw new AppException(62300, "Không tìm thấy bình luận", HttpStatus.NOT_FOUND);
		}

		result.put("comment", comment);

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
}