package hcmus.alumni.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.event.dto.ICommentEventDto;
import hcmus.alumni.event.model.CommentEventModel;

public interface CommentEventRepository extends JpaRepository<CommentEventModel, String> {
	@Query(value = "select count(*) > 0 from comment_event where id = :commentId and creator = :userId and is_delete = false", nativeQuery = true)
	Long isCommentOwner(String commentId, String userId);
	
	@Query("SELECT new CommentEventModel(c, :userId, :canDelete) FROM CommentEventModel c " +
	        "WHERE c.event.id = :eventId AND c.id = :commentId AND c.isDelete = false")
	Optional<ICommentEventDto> getComment(String eventId, String commentId, String userId, boolean canDelete);
	
	@Query("SELECT new CommentEventModel(c, :userId, :canDelete) FROM CommentEventModel c " +
	        "WHERE c.event.id = :eventId AND c.isDelete = false AND c.parentId IS NULL")
	Page<ICommentEventDto> getComments(String eventId, String userId, boolean canDelete,Pageable pageable);
	
	@Query("SELECT new CommentEventModel(c, :userId, :canDelete) FROM CommentEventModel c " +
	        "WHERE c.isDelete = false AND c.parentId = :commentId")
	Page<ICommentEventDto> getChildrenComment(String commentId, String userId, boolean canDelete,Pageable pageable);
	
	@Query("SELECT c FROM CommentEventModel c WHERE c.isDelete = false AND c.parentId = :commentId")
	List<CommentEventModel> getChildrenComment(String commentId);
	
	@Transactional
	@Modifying
	@Query("UPDATE CommentEventModel c SET c.content = :content WHERE c.id = :commentId AND c.creator.id = :creator")
	int updateComment(@Param("commentId") String commentId, @Param("creator") String creator, @Param("content") String content);
	
	@Transactional
	@Modifying
	@Query("UPDATE CommentEventModel c SET c.isDelete = true WHERE c.id = :commentId")
	int deleteComment(@Param("commentId") String commentId);
	
	@Transactional
	@Modifying
	@Query("UPDATE CommentEventModel c SET c.isDelete = true WHERE c.parentId = :parentId AND c.isDelete = false")
	int deleteChildrenComment(@Param("parentId") String parentId);
	
	@Transactional
	@Modifying
	@Query("UPDATE CommentEventModel c SET c.childrenCommentNumber = c.childrenCommentNumber + :count WHERE c.id = :id")
	int commentCountIncrement(String id,@Param("count") Integer count);
	
	@Query("SELECT c.id FROM CommentEventModel c WHERE c.event.id = :eventId")
	List<String> findByEventId(@Param("eventId") String eventId);
	
	@Query("SELECT c.id FROM CommentEventModel c WHERE c.parentId IN :parentIds")
	List<String> findByParentIds(List<String> parentIds);
}
