package hcmus.alumni.counsel.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
import hcmus.alumni.counsel.dto.ICommentPostAdviseDto;
import hcmus.alumni.counsel.dto.IPostAdviseDto;
import hcmus.alumni.counsel.model.CommentPostAdviseModel;
import hcmus.alumni.counsel.model.PostAdviseModel;
import hcmus.alumni.counsel.model.StatusPostModel;
import hcmus.alumni.counsel.model.UserModel;
import hcmus.alumni.counsel.repository.CommentPostAdviseRepository;
import hcmus.alumni.counsel.repository.PostAdviseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/counsel")
public class CounselServiceController {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private PostAdviseRepository postAdviseRepository;
	@Autowired
	private CommentPostAdviseRepository commentPostAdviseRepository;

	// @GetMapping("/count")
	// public ResponseEntity<Long> getNewsCount(
	// @RequestParam(value = "statusId", defaultValue = "0") Integer statusId) {
	// if (statusId.equals(0)) {
	// return
	// ResponseEntity.status(HttpStatus.OK).body(postAdviseRepository.getCountByNotDelete());
	// }
	// return
	// ResponseEntity.status(HttpStatus.OK).body(postAdviseRepository.getCountByStatusId(statusId));
	// }

	@GetMapping("")
	public ResponseEntity<HashMap<String, Object>> getNews(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize,
					Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<IPostAdviseDto> posts = postAdviseRepository.searchPostAdvise(title, pageable);

			result.put("totalPages", posts.getTotalPages());
			result.put("posts", posts.getContent());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<IPostAdviseDto> getNewsById(@PathVariable String id) {
		Optional<IPostAdviseDto> optionalPost = postAdviseRepository.findPostAdviseById(id);
		if (optionalPost.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.status(HttpStatus.OK).body(optionalPost.get());
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PostMapping("")
	public ResponseEntity<String> createPostAdvise(@RequestHeader("userId") String creator,
			@RequestBody PostAdviseModel reqPostAdvise) {
		if (creator.isEmpty() || reqPostAdvise.getTitle().isEmpty() || reqPostAdvise.getContent().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields must not be empty");
		}
		PostAdviseModel postAdvise = new PostAdviseModel(creator, reqPostAdvise.getTitle(), reqPostAdvise.getContent(),
				reqPostAdvise.getTags());
		postAdvise.setPublishedAt(new Date());
		postAdviseRepository.save(postAdvise);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("hasAnyAuthority('Alumni', 'Lecturer')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updatePostAdvise(
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
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteNews(
			@RequestHeader("userId") String creator,
			@PathVariable String id) {
		// Find advise post
		Optional<PostAdviseModel> optionalPostAdvise = postAdviseRepository.findById(id);
		if (optionalPostAdvise.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post advise not found");
		}

		PostAdviseModel postAdivse = optionalPostAdvise.get();
		// Check if user is creator
		if (!postAdivse.getCreator().getId().equals(creator)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the creator of this post");
		}

		postAdivse.setStatus(new StatusPostModel(4));
		postAdviseRepository.save(postAdivse);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	// Get comments of a news
	@GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getNewsComments(@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize,
				Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentPostAdviseDto> comments = commentPostAdviseRepository.getComments(id, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get children comments of a comment
	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getNewsChildrenComments(
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
		Page<ICommentPostAdviseDto> comments = commentPostAdviseRepository.getChildrenComment(commentId, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PostMapping("/{id}/comments")
	public ResponseEntity<String> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentPostAdviseModel comment) {
		comment.setId(UUID.randomUUID().toString());
		comment.setPostAdvise(new PostAdviseModel(id));
		comment.setCreator(new UserModel(creator));
		System.out.println(comment.toString());

		if (comment.getParentId() != null) {
			commentPostAdviseRepository.commentCountIncrement(comment.getParentId(), 1);
		}

		commentPostAdviseRepository.save(comment);
		postAdviseRepository.commentCountIncrement(id, 1);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId, @RequestBody CommentPostAdviseModel updatedComment) {
		if (updatedComment.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
		}
		int updates = commentPostAdviseRepository.updateComment(commentId, creator,
				updatedComment.getContent());
		if (updates == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

	@DeleteMapping("/comments/{commentId}")
	public ResponseEntity<String> deleteComment(
			@RequestHeader("userId") String creator,
			@PathVariable String commentId) {
		// Check if comment exists
		Optional<CommentPostAdviseModel> originalComment = commentPostAdviseRepository.findById(commentId);
		if (originalComment.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
		}
		String postId = originalComment.get().getPostAdvise().getId();
		if (originalComment.get().getParentId() != null) {
			commentPostAdviseRepository.commentCountIncrement(originalComment.get().getParentId(), -1);
		}

		// Initilize variables
		List<CommentPostAdviseModel> childrenComments = new ArrayList<CommentPostAdviseModel>();
		List<String> allParentId = new ArrayList<String>();
		int totalDelete = 1;

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
		commentPostAdviseRepository.deleteComment(commentId, creator);
		for (String parentId : allParentId) {
			totalDelete += commentPostAdviseRepository.deleteChildrenComment(parentId);
		}
		postAdviseRepository.commentCountIncrement(postId, -totalDelete);

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
