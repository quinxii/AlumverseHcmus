package hcmus.alumni.counsel.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.web.multipart.MultipartFile;

import hcmus.alumni.counsel.dto.ICommentPostAdviseDto;
import hcmus.alumni.counsel.dto.IPostAdviseDto;
import hcmus.alumni.counsel.model.CommentPostAdviseModel;
import hcmus.alumni.counsel.model.PostAdviseModel;
import hcmus.alumni.counsel.model.StatusPostModel;
import hcmus.alumni.counsel.model.UserModel;
import hcmus.alumni.counsel.repository.CommentPostAdviseRepository;
import hcmus.alumni.counsel.repository.PostAdviseRepository;
import hcmus.alumni.counsel.utils.ImageUtils;
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
	@Autowired
	private ImageUtils imageUtils;

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

	// @GetMapping("")
	// public ResponseEntity<HashMap<String, Object>> getNews(
	// @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	// @RequestParam(value = "pageSize", required = false, defaultValue = "10") int
	// pageSize,
	// @RequestParam(value = "title", required = false, defaultValue = "") String
	// title,
	// @RequestParam(value = "orderBy", required = false, defaultValue =
	// "publishedAt") String orderBy,
	// @RequestParam(value = "order", required = false, defaultValue = "desc")
	// String order,
	// @RequestParam(value = "statusId", required = false, defaultValue = "0")
	// Integer statusId) {
	// if (pageSize == 0 || pageSize > 50) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	// }
	// HashMap<String, Object> result = new HashMap<String, Object>();

	// try {
	// Pageable pageable = PageRequest.of(page, pageSize,
	// Sort.by(Sort.Direction.fromString(order), orderBy));
	// Page<IPostAdviseDto> news = null;
	// if (statusId.equals(0)) {
	// news = postAdviseRepository.searchNews(title, pageable);
	// } else {
	// news = postAdviseRepository.searchNewsByStatus(title, statusId, pageable);
	// }

	// result.put("totalPages", news.getTotalPages());
	// result.put("news", news.getContent());
	// } catch (IllegalArgumentException e) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	// } catch (Exception e) {
	// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	// }

	// return ResponseEntity.status(HttpStatus.OK).body(result);
	// }

	// @GetMapping("/{id}")
	// public ResponseEntity<IPostAdviseDto> getNewsById(@PathVariable String id) {
	// Optional<IPostAdviseDto> optionalNews =
	// postAdviseRepository.findNewsById(id);
	// if (optionalNews.isEmpty()) {
	// return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	// }
	// postAdviseRepository.viewsIncrement(id);
	// return ResponseEntity.status(HttpStatus.OK).body(optionalNews.get());
	// }

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
		// System.out.println(updatedPostAdvise.toString());

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

	// @PreAuthorize("hasAnyAuthority('Admin')")
	// @DeleteMapping("/{id}")
	// public ResponseEntity<String> deleteNews(@PathVariable String id) {
	// // Find news
	// Optional<PostAdviseModel> optionalNews = postAdviseRepository.findById(id);
	// if (optionalNews.isEmpty()) {
	// return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
	// }
	// PostAdviseModel news = optionalNews.get();
	// news.setStatus(new StatusPostModel(4));
	// postAdviseRepository.save(news);
	// return ResponseEntity.status(HttpStatus.OK).body("");
	// }

	// @PreAuthorize("hasAnyAuthority('Admin')")
	// @PutMapping("/{id}/content")
	// public ResponseEntity<String> updateNewsContent(@PathVariable String id,
	// @RequestBody(required = false) PostAdviseModel updatedNews) {
	// if (updatedNews.getContent() == null) {
	// return ResponseEntity.status(HttpStatus.OK).body("");
	// }
	// // Find news
	// Optional<PostAdviseModel> optionalNews = postAdviseRepository.findById(id);
	// if (optionalNews.isEmpty()) {
	// return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
	// }
	// PostAdviseModel news = optionalNews.get();
	// if (!updatedNews.getContent().equals("")) {
	// String newContent = imageUtils.updateImgFromHtmlToStorage(news.getContent(),
	// updatedNews.getContent(), id);
	// if (newContent.equals(news.getContent())) {
	// return ResponseEntity.status(HttpStatus.OK).body("");
	// }
	// news.setContent(newContent);
	// postAdviseRepository.save(news);
	// }
	// return ResponseEntity.status(HttpStatus.OK).body("");
	// }

	// @GetMapping("/most-viewed")
	// public ResponseEntity<HashMap<String, Object>> getMostViewed(
	// @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
	// if (limit <= 0 || limit > 5) {
	// limit = 5;
	// }
	// Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC,
	// "views"));
	// Page<IPostAdviseDto> news = postAdviseRepository.getMostViewdNews(pageable);

	// HashMap<String, Object> result = new HashMap<String, Object>();
	// result.put("news", news.getContent());

	// return ResponseEntity.status(HttpStatus.OK).body(result);
	// }

	// @GetMapping("/hot")
	// public ResponseEntity<HashMap<String, Object>> getHotNews(
	// @RequestParam(value = "limit", defaultValue = "4") Integer limit) {
	// if (limit <= 0 || limit > 5) {
	// limit = 5;
	// }
	// Calendar cal = Calendar.getInstance();
	// Date endDate = cal.getTime();
	// cal.add(Calendar.WEEK_OF_YEAR, -1);
	// Date startDate = cal.getTime();

	// Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC,
	// "views"));
	// Page<IPostAdviseDto> news = postAdviseRepository.getHotNews(startDate,
	// endDate, pageable);

	// HashMap<String, Object> result = new HashMap<String, Object>();
	// result.put("news", news.getContent());

	// return ResponseEntity.status(HttpStatus.OK).body(result);
	// }

	// // Get comments of a news
	// // @PreAuthorize("hasAnyAuthority('Cựu sinh viên')")
	// @GetMapping("/{id}/comments")
	// public ResponseEntity<HashMap<String, Object>> getNewsComments(@PathVariable
	// String id,
	// @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	// @RequestParam(value = "pageSize", required = false, defaultValue = "10") int
	// pageSize) {
	// if (pageSize == 0 || pageSize > 50) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	// }
	// HashMap<String, Object> result = new HashMap<String, Object>();

	// Pageable pageable = PageRequest.of(page, pageSize,
	// Sort.by(Sort.Direction.DESC, "createAt"));
	// Page<ICommentPostAdviseDto> comments =
	// commentPostAdviseRepository.getComments(id, pageable);

	// result.put("comments", comments.getContent());

	// return ResponseEntity.status(HttpStatus.OK).body(result);
	// }

	// // Get children comments of a comment
	// // @PreAuthorize("hasAnyAuthority('Cựu sinh viên')")
	// @GetMapping("/comments/{commentId}/children")
	// public ResponseEntity<HashMap<String, Object>> getNewsChildrenComments(
	// @PathVariable String commentId,
	// @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	// @RequestParam(value = "pageSize", required = false, defaultValue = "10") int
	// pageSize) {
	// if (pageSize == 0 || pageSize > 50) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	// }
	// HashMap<String, Object> result = new HashMap<String, Object>();

	// // Check if parent comment deleted
	// Optional<CommentPostAdviseModel> parentComment =
	// commentPostAdviseRepository.findById(commentId);
	// if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	// }

	// Pageable pageable = PageRequest.of(page, pageSize,
	// Sort.by(Sort.Direction.DESC, "createAt"));
	// Page<ICommentPostAdviseDto> comments =
	// commentPostAdviseRepository.getChildrenComment(commentId, pageable);

	// result.put("comments", comments.getContent());

	// return ResponseEntity.status(HttpStatus.OK).body(result);
	// }

	// // @PreAuthorize("hasAnyAuthority('Cựu sinh viên')")
	// @PostMapping("/{id}/comments")
	// public ResponseEntity<String> createComment(
	// @RequestHeader("userId") String creator,
	// @PathVariable String id, @RequestBody CommentPostAdviseModel comment) {
	// comment.setId(UUID.randomUUID().toString());
	// comment.setNews(new PostAdviseModel(id));
	// comment.setCreator(new UserModel(creator));
	// commentPostAdviseRepository.save(comment);
	// postAdviseRepository.commentCountIncrement(id, 1);
	// return ResponseEntity.status(HttpStatus.CREATED).body(null);
	// }

	// // @PreAuthorize("hasAnyAuthority('Cựu sinh viên')")
	// @PutMapping("/comments/{commentId}")
	// public ResponseEntity<String> updateComment(
	// @RequestHeader("userId") String creator,
	// @PathVariable String commentId, @RequestBody CommentPostAdviseModel
	// updatedComment) {
	// if (updatedComment.getContent() == null) {
	// return ResponseEntity.status(HttpStatus.OK).body("");
	// }
	// int updates = commentPostAdviseRepository.updateComment(commentId, creator,
	// updatedComment.getContent());
	// if (updates == 0) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
	// }
	// return ResponseEntity.status(HttpStatus.OK).body(null);
	// }

	// // @PreAuthorize("hasAnyAuthority('Cựu sinh viên')")
	// @DeleteMapping("/comments/{commentId}")
	// public ResponseEntity<String> deleteComment(
	// @RequestHeader("userId") String creator,
	// @PathVariable String commentId) {
	// // Check if comment exists
	// Optional<CommentPostAdviseModel> originalComment =
	// commentPostAdviseRepository.findById(commentId);
	// if (originalComment.isEmpty()) {
	// return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
	// }
	// String newsId = originalComment.get().getNews().getId();

	// // Initilize variables
	// List<CommentPostAdviseModel> childrenComments = new
	// ArrayList<CommentPostAdviseModel>();
	// List<String> allParentId = new ArrayList<String>();
	// int totalDelete = 1;

	// // Get children comments
	// String curCommentId = commentId;
	// allParentId.add(curCommentId);
	// childrenComments.addAll(commentPostAdviseRepository.getChildrenComment(curCommentId));

	// // Start the loop
	// while (!childrenComments.isEmpty()) {
	// CommentPostAdviseModel curComment = childrenComments.get(0);
	// curCommentId = curComment.getId();
	// List<CommentPostAdviseModel> temp =
	// commentPostAdviseRepository.getChildrenComment(curCommentId);
	// if (!temp.isEmpty()) {
	// allParentId.add(curCommentId);
	// childrenComments.addAll(temp);
	// }

	// childrenComments.remove(0);
	// }

	// // Delete all comments and update comment count
	// commentPostAdviseRepository.deleteComment(commentId, creator);
	// for (String parentId : allParentId) {
	// totalDelete += commentPostAdviseRepository.deleteChildrenComment(parentId);
	// }
	// postAdviseRepository.commentCountIncrement(newsId, -totalDelete);

	// return ResponseEntity.status(HttpStatus.OK).body(null);
	// }
}
