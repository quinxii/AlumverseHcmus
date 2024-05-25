package hcmus.alumni.counsel.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.query.sqm.UnknownPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import hcmus.alumni.counsel.dto.ICommentPostAdviseDto;
import hcmus.alumni.counsel.dto.IInteractPostAdviseDto;
import hcmus.alumni.counsel.dto.IPostAdviseDto;
import hcmus.alumni.counsel.dto.ReactRequestDto;
import hcmus.alumni.counsel.model.CommentPostAdviseModel;
import hcmus.alumni.counsel.model.InteractPostAdviseId;
import hcmus.alumni.counsel.model.InteractPostAdviseModel;
import hcmus.alumni.counsel.model.PicturePostAdviseModel;
import hcmus.alumni.counsel.model.PostAdviseModel;
import hcmus.alumni.counsel.model.ReactModel;
import hcmus.alumni.counsel.model.StatusPostModel;
import hcmus.alumni.counsel.model.UserModel;
import hcmus.alumni.counsel.repository.CommentPostAdviseRepository;
import hcmus.alumni.counsel.repository.InteractPostAdviseRepository;
import hcmus.alumni.counsel.repository.PostAdviseRepository;
import hcmus.alumni.counsel.utils.ImageUtils;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/counsel")
public class CounselServiceController {
	@Autowired
	private PostAdviseRepository postAdviseRepository;
	@Autowired
	private CommentPostAdviseRepository commentPostAdviseRepository;
	@Autowired
	private InteractPostAdviseRepository interactPostAdviseRepository;
	@Autowired
	private ImageUtils imageUtils;

	private final int MAX_IMAGE_SIZE_PER_POST = 5;

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getPosts(
			@RequestHeader("userId") String userId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "tagsId", required = false) List<Integer> tagsId) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<IPostAdviseDto> posts = postAdviseRepository.searchPostAdvise(title, userId, tagsId, pageable);

			result.put("totalPages", posts.getTotalPages());
			result.put("posts", posts.getContent());
		} catch (IllegalArgumentException | UnknownPathException | InvalidDataAccessApiUsageException e) {
			System.err.println(e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			System.err.println(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<IPostAdviseDto> getPostById(
			@RequestHeader("userId") String userId,
			@PathVariable String id) {
		Optional<IPostAdviseDto> optionalPost = postAdviseRepository.findPostAdviseById(id, userId);
		if (optionalPost.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalPost.get());
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PostMapping("")
	public ResponseEntity<Map<String, Object>> createPostAdvise(@RequestHeader("userId") String creator,
			@RequestBody PostAdviseModel reqPostAdvise) {
		if (creator.isEmpty() || reqPostAdvise.getTitle().isEmpty() || reqPostAdvise.getContent().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Collections.singletonMap("msg", "Title and content must not be empty"));
		}
		PostAdviseModel postAdvise = new PostAdviseModel(creator, reqPostAdvise.getTitle(), reqPostAdvise.getContent(),
				reqPostAdvise.getTags());
		postAdvise.setPublishedAt(new Date());
		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonMap("id", postAdvise.getId()));
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updatePost(
			@RequestHeader("userId") String creator,
			@PathVariable String id,
			@RequestBody PostAdviseModel updatedPostAdvise) {
		if (creator.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields must not be empty");
		}

		Optional<PostAdviseModel> optionalPostAdvise = postAdviseRepository.findById(id);
		if (optionalPostAdvise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
		}

		PostAdviseModel postAdvise = optionalPostAdvise.get();

		// Check if user is creator
		if (!postAdvise.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this post");
		}

		if (updatedPostAdvise.getTitle() != null && !updatedPostAdvise.getTitle().isEmpty()) {
			postAdvise.setTitle(updatedPostAdvise.getTitle());
		}
		if (updatedPostAdvise.getContent() != null && !updatedPostAdvise.getContent().isEmpty()) {
			postAdvise.setContent(updatedPostAdvise.getContent());
		}
		if (updatedPostAdvise.getTags() != null) {
			postAdvise.setTags(updatedPostAdvise.getTags());
		}

		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PutMapping("/{id}/images")
	public ResponseEntity<String> createPostAdviseImages(@RequestHeader("userId") String creator,
			@PathVariable String id,
			@RequestParam(value = "addedImages", required = false) List<MultipartFile> addedImages,
			@RequestParam(value = "deletedImageIds", required = false) List<String> deletedImageIds) {
		if (addedImages == null && deletedImageIds == null) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		Optional<PostAdviseModel> optionalPostAdvise = postAdviseRepository.findById(id);
		if (optionalPostAdvise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid post id");
		}

		PostAdviseModel postAdvise = optionalPostAdvise.get();
		// Check if user is creator
		
		if (!postAdvise.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this post");
		}
		List<PicturePostAdviseModel> images = postAdvise.getPictures();

		if (addedImages != null && deletedImageIds != null
				&& images.size() + addedImages.size() - deletedImageIds.size() > MAX_IMAGE_SIZE_PER_POST) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exceed images can be uploaded per post");
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
					String pictureUrl = imageUtils.saveImageToStorage(imageUtils.getCounselPath(id),
							addedImages.get(i),
							pictureId);
					postAdvise.getPictures()
							.add(new PicturePostAdviseModel(pictureId, postAdvise, pictureUrl, order));
				}
			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving images");
			}
		}

		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deletePost(
			@RequestHeader("userId") String creator,
			@PathVariable String id) {
		// Find advise post
		Optional<PostAdviseModel> optionalPostAdvise = postAdviseRepository.findById(id);
		if (optionalPostAdvise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post advise not found");
		}

		PostAdviseModel postAdvise = optionalPostAdvise.get();
		// Check if user is creator
		if (!postAdvise.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this post");
		}

		postAdvise.getPictures().clear();
		postAdvise.setStatus(new StatusPostModel(4));
		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	// Get comments of a news
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

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostAdviseDto> comments = commentPostAdviseRepository.getComments(id, userId, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get children comments of a comment
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
		Optional<CommentPostAdviseModel> parentComment = commentPostAdviseRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostAdviseDto> comments = commentPostAdviseRepository.getChildrenComment(commentId, userId,
				pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PostMapping("/{id}/comments")
	public ResponseEntity<String> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentPostAdviseModel comment) {
		comment.setId(UUID.randomUUID().toString());
		comment.setPostAdvise(new PostAdviseModel(id));
		comment.setCreator(new UserModel(creator));
		commentPostAdviseRepository.save(comment);

		if (comment.getParentId() != null) {
			commentPostAdviseRepository.commentCountIncrement(comment.getParentId(), 1);
		} else {
			postAdviseRepository.commentCountIncrement(id, 1);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId, @RequestBody CommentPostAdviseModel updatedComment) {
		if (updatedComment.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}

		// Check if user is comment's creator
		Optional<CommentPostAdviseModel> optionalComment = commentPostAdviseRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
		}
		CommentPostAdviseModel comment = optionalComment.get();
		if (!comment.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this comment");
		}

		commentPostAdviseRepository.updateComment(commentId, creator, updatedComment.getContent());
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentPostAdviseModel> optionalComment = commentPostAdviseRepository.findById(commentId);
		if (optionalComment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		CommentPostAdviseModel originalComment = optionalComment.get();
		// Check if user is comment's creator
		if (!originalComment.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this comment");
		}

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
		int deleted = commentPostAdviseRepository.deleteComment(commentId, creator);
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

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@GetMapping("/{id}/react")
	public ResponseEntity<HashMap<String, Object>> getPostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestParam Integer reactId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString("desc"), "createAt"));
			Page<IInteractPostAdviseDto> users = interactPostAdviseRepository.getReactionUsers(id, reactId,
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
	public ResponseEntity<String> postPostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		Optional<InteractPostAdviseModel> optionalInteractPostAdvise = interactPostAdviseRepository
				.findById(new InteractPostAdviseId(id, creatorId));

		if (!optionalInteractPostAdvise.isEmpty() && optionalInteractPostAdvise.get().getIsDelete() == false) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have already reacted to this post");
		}

		InteractPostAdviseModel interactPostAdvise = new InteractPostAdviseModel(id, creatorId,
				req.getReactId());
		interactPostAdviseRepository.save(interactPostAdvise);
		postAdviseRepository.reactionCountIncrement(id, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PutMapping("/{id}/react")
	public ResponseEntity<String> putPostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id,
			@RequestBody ReactRequestDto req) {
		Optional<InteractPostAdviseModel> optionalInteractPostAdvise = interactPostAdviseRepository
				.findById(new InteractPostAdviseId(id, creatorId));

		if (optionalInteractPostAdvise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not found");
		}

		InteractPostAdviseModel interactPostAdvise = optionalInteractPostAdvise.get();
		if (interactPostAdvise.getReact().getId() == req.getReactId()) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}

		interactPostAdvise.setReact(new ReactModel(req.getReactId()));
		interactPostAdviseRepository.save(interactPostAdvise);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@DeleteMapping("/{id}/react")
	public ResponseEntity<String> deletePostAdviseReaction(@RequestHeader("userId") String creatorId,
			@PathVariable String id) {
		Optional<InteractPostAdviseModel> optionalInteractPostAdvise = interactPostAdviseRepository
				.findById(new InteractPostAdviseId(id, creatorId));

		if (optionalInteractPostAdvise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not found");
		}

		InteractPostAdviseModel interactPostAdvise = optionalInteractPostAdvise.get();
		interactPostAdvise.setIsDelete(true);
		postAdviseRepository.reactionCountIncrement(id, -1);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
