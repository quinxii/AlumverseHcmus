package hcmus.alumni.counsel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.counsel.dto.response.ICommentPostAdviseDto;
import hcmus.alumni.counsel.dto.response.ICommentWithPostDto;
import hcmus.alumni.counsel.model.CommentPostAdviseModel;

public interface CommentPostAdviseRepository extends JpaRepository<CommentPostAdviseModel, String> {
    @Query(value = "select count(*) > 0 from comment_post_advise where id = :commentId and creator = :userId and is_delete = false", nativeQuery = true)
    Long isCommentOwner(String commentId, String userId);

    @Query("SELECT new CommentPostAdviseModel(c, :userId, :canDelete) FROM CommentPostAdviseModel c " +
            "WHERE c.postAdvise.id = :postId AND c.id = :commentId AND c.isDelete = false")
    Optional<ICommentPostAdviseDto> getComment(String postId, String commentId, String userId, boolean canDelete);

    @Query("SELECT new CommentPostAdviseModel(c, :userId, :canDelete) FROM CommentPostAdviseModel c " +
            "WHERE c.postAdvise.id = :postAdviseId AND c.isDelete = false AND c.parentId IS NULL")
    Page<ICommentPostAdviseDto> getComments(String postAdviseId, String userId, boolean canDelete, Pageable pageable);

    @Query("SELECT new CommentPostAdviseModel(c, :userId, :canDelete) FROM CommentPostAdviseModel c " +
            "WHERE c.creator.id = :userId AND c.isDelete = false")
    Page<ICommentWithPostDto> getCommentsByUserId(String userId, boolean canDelete, Pageable pageable);

    @Query("SELECT new CommentPostAdviseModel(c, :userId, :canDelete) FROM CommentPostAdviseModel c " +
            "WHERE c.isDelete = false AND c.parentId = :commentId")
    Page<ICommentPostAdviseDto> getChildrenComment(String commentId, String userId, boolean canDelete,
            Pageable pageable);

    @Query("SELECT c FROM CommentPostAdviseModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    List<CommentPostAdviseModel> getChildrenComment(String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.content = :content WHERE c.id = :commentId AND c.creator.id = :creator")
    int updateComment(@Param("commentId") String commentId, @Param("creator") String creator,
            @Param("content") String content);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.isDelete = true WHERE c.id = :commentId AND c.isDelete = false")
    int deleteComment(@Param("commentId") String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.isDelete = true WHERE c.parentId = :parentId AND c.isDelete = false")
    int deleteChildrenComment(@Param("parentId") String parentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.childrenCommentNumber = c.childrenCommentNumber + :count WHERE c.id = :id")
    int commentCountIncrement(String id, @Param("count") Integer count);
    
	@Query("SELECT c.id FROM CommentPostAdviseModel c WHERE c.postAdvise.id = :postAdviseId")
	List<String> findByPostAdviseId(@Param("postAdviseId") String postAdviseId);
	
	@Query("SELECT c.id FROM CommentPostAdviseModel c WHERE c.parentId IN :parentIds")
	List<String> findByParentIds(List<String> parentIds);
}
