package hcmus.alumni.news.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

import hcmus.alumni.news.dto.ICommentNewsDto;
import hcmus.alumni.news.dto.INewsDto;
import hcmus.alumni.news.model.CommentNewsModel;
import hcmus.alumni.news.model.FacultyModel;
import hcmus.alumni.news.model.NewsModel;
import hcmus.alumni.news.model.StatusPostModel;
import hcmus.alumni.news.model.TagModel;
import hcmus.alumni.news.model.UserModel;
import hcmus.alumni.news.repository.CommentNewsRepository;
import hcmus.alumni.news.repository.NewsRepository;
import hcmus.alumni.news.utils.ImageUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/news")
public class NewsServiceController {
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private NewsRepository newsRepository;
	@Autowired
	private CommentNewsRepository commentNewsRepository;
	@Autowired
	private ImageUtils imageUtils;

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
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
			@RequestParam(value = "title", required = false, defaultValue = "") String title,
			@RequestParam(value = "orderBy", required = false, defaultValue = "publishedAt") String orderBy,
			@RequestParam(value = "order", required = false, defaultValue = "desc") String order,
			@RequestParam(value = "facultyId", required = false) Integer facultyId,
			@RequestParam(value = "tagsId", required = false) List<Integer> tagsId,
			@RequestParam(value = "statusId", required = false) Integer statusId) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		try {
			Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.fromString(order), orderBy));
			Page<INewsDto> news = null;

			news = newsRepository.searchNews(title, facultyId, tagsId, statusId, pageable);

			result.put("totalPages", news.getTotalPages());
			result.put("news", news.getContent());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@GetMapping("/{id}")
	public ResponseEntity<INewsDto> getNewsById(@PathVariable String id) {
		Optional<INewsDto> optionalNews = newsRepository.findNewsById(id);
		if (optionalNews.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		newsRepository.viewsIncrement(id);
		return ResponseEntity.status(HttpStatus.OK).body(optionalNews.get());
	}

	@PreAuthorize("hasAnyAuthority('News.Create')")
	@PostMapping("")
	public ResponseEntity<String> createNews(@RequestHeader("userId") String creator,
			@RequestParam(value = "title") String title, @RequestParam(value = "thumbnail") MultipartFile thumbnail,
			@RequestParam(value = "summary", required = false, defaultValue = "") String summary,
			@RequestParam(value = "tagsId[]", required = false, defaultValue = "") Integer[] tagsId,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
			@RequestParam(value = "scheduledTime", required = false) Long scheduledTimeMili) {
		if (creator.isEmpty() || title.isEmpty() || thumbnail.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("All fields must not be empty");
		}
		if (thumbnail.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
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
			if (tagsId != null) {
				news.setTags(tagsId);
			}
			if (!facultyId.equals(0)) {
				news.setFaculty(new FacultyModel(facultyId));
			}
			newsRepository.save(news);
		} catch (IOException e) {
			System.err.println(e);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(id);
	}

	@PreAuthorize("hasAnyAuthority('News.Edit')")
	@PutMapping("/{id}")
	public ResponseEntity<String> updateNews(@PathVariable String id,
			@RequestParam(value = "title", defaultValue = "") String title,
			@RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
			@RequestParam(value = "summary", required = false, defaultValue = "") String summary,
			@RequestParam(value = "tagsId[]", required = false, defaultValue = "") Integer[] tagsId,
			@RequestParam(value = "facultyId", required = false, defaultValue = "0") Integer facultyId,
			@RequestParam(value = "statusId", required = false, defaultValue = "0") Integer statusId) {
		if (thumbnail != null && thumbnail.getSize() > 5 * 1024 * 1024) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be lower than 5MB");
		}
		boolean isPut = false;

		try {
			// Find news
			Optional<NewsModel> optionalNews = newsRepository.findById(id);
			if (optionalNews.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
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
			if (tagsId != null) {
				news.setTags(tagsId);
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
			System.err.println(e);
		}
		return ResponseEntity.status(HttpStatus.OK).body("");
	}

	@PreAuthorize("hasAnyAuthority('News.Delete')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteNews(@PathVariable String id) {
		// Find news
		Optional<NewsModel> optionalNews = newsRepository.findById(id);
		if (optionalNews.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
		}
		NewsModel news = optionalNews.get();
		news.setStatus(new StatusPostModel(4));
		newsRepository.save(news);
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid id");
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
	public ResponseEntity<HashMap<String, Object>> getMostViewed(
			@PathVariable String id,
			@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
		if (limit <= 0 || limit > 10) {
			limit = 10;
		}
		Optional<NewsModel> optionalNews = newsRepository.findById(id);
		if (optionalNews.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		FacultyModel faculty = optionalNews.get().getFaculty();
		Integer facultyId = null;
		if (faculty != null) {
			facultyId = optionalNews.get().getFaculty().getId();
		}
		List<Integer> tagsId = optionalNews.get().getTags().stream().map(TagModel::getId).collect(Collectors.toList());

		Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
		Page<INewsDto> news = newsRepository.getRelatedNews(id, facultyId, tagsId, pageable);

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
		Page<INewsDto> news = newsRepository.getHotNews(startDate, endDate, pageable);

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("news", news.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get comments of a news
	@GetMapping("/{id}/comments")
	public ResponseEntity<HashMap<String, Object>> getNewsComments(
			@RequestHeader("userId") String userId,
			@PathVariable String id,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentNewsDto> comments = commentNewsRepository.getComments(id, userId, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// Get children comments of a comment
	@GetMapping("/comments/{commentId}/children")
	public ResponseEntity<HashMap<String, Object>> getNewsChildrenComments(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId,
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {
		if (pageSize == 0 || pageSize > 50) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
		HashMap<String, Object> result = new HashMap<String, Object>();

		// Check if parent comment deleted
		Optional<CommentNewsModel> parentComment = commentNewsRepository.findById(commentId);
		if (parentComment.isEmpty() || parentComment.get().getIsDelete()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));
		Page<ICommentNewsDto> comments = commentNewsRepository.getChildrenComment(commentId, userId, pageable);

		result.put("comments", comments.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@PreAuthorize("hasAnyAuthority('News.Comment.Create')")
	@PostMapping("/{id}/comments")
	public ResponseEntity<String> createComment(
			@RequestHeader("userId") String creator,
			@PathVariable String id, @RequestBody CommentNewsModel comment) {
		comment.setId(UUID.randomUUID().toString());
		comment.setNews(new NewsModel(id));
		comment.setCreator(new UserModel(creator));
		commentNewsRepository.save(comment);

		if (comment.getParentId() != null) {
			commentNewsRepository.commentCountIncrement(comment.getParentId(), 1);
		} else {
			newsRepository.commentCountIncrement(id, 1);
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}

	@PreAuthorize("1 == @commentNewsRepository.isCommentOwner(#commentId, #userId)")
	@PutMapping("/comments/{commentId}")
	public ResponseEntity<String> updateComment(
			@RequestHeader("userId") String userId,
			@PathVariable String commentId, @RequestBody CommentNewsModel updatedComment) {
		if (updatedComment.getContent() == null) {
			return ResponseEntity.status(HttpStatus.OK).body("");
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
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id");
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
		int deleted = commentNewsRepository.deleteComment(commentId, userId);
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

		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
}
