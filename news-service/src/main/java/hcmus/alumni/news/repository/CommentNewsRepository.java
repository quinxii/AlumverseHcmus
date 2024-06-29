package hcmus.alumni.news.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.news.dto.ICommentNewsDto;
import hcmus.alumni.news.model.CommentNewsModel;

public interface CommentNewsRepository extends JpaRepository<CommentNewsModel, String> {
    @Query(value = "select count(*) > 0 from comment_news where id = :commentId and creator = :userId and is_delete = false", nativeQuery = true)
    Long isCommentOwner(String commentId, String userId);

    @Query("SELECT new CommentNewsModel(c, :userId, :canDelete) FROM CommentNewsModel c " +
            "WHERE c.news.id = :newsId AND c.id = :commentId AND c.isDelete = false")
    Optional<ICommentNewsDto> getComment(String newsId, String commentId, String userId, boolean canDelete);

    @Query("SELECT new CommentNewsModel(c, :userId, :canDelete) FROM CommentNewsModel c " +
            "WHERE c.news.id = :newsId AND c.isDelete = false AND c.parentId IS NULL")
    Page<ICommentNewsDto> getComments(String newsId, String userId, boolean canDelete, Pageable pageable);

    @Query("SELECT new CommentNewsModel(c, :userId, :canDelete) FROM CommentNewsModel c " +
            "WHERE c.isDelete = false AND c.parentId = :commentId")
    Page<ICommentNewsDto> getChildrenComment(String commentId, String userId, boolean canDelete, Pageable pageable);

    @Query("SELECT c FROM CommentNewsModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    List<CommentNewsModel> getChildrenComment(String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentNewsModel c SET c.content = :content WHERE c.id = :commentId AND c.creator.id = :creator")
    int updateComment(@Param("commentId") String commentId, @Param("creator") String creator,
            @Param("content") String content);

    @Transactional
    @Modifying
    @Query("UPDATE CommentNewsModel c SET c.isDelete = true WHERE c.id = :commentId AND c.isDelete = false")
    int deleteComment(@Param("commentId") String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentNewsModel c SET c.isDelete = true WHERE c.parentId = :parentId AND c.isDelete = false")
    int deleteChildrenComment(@Param("parentId") String parentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentNewsModel c SET c.childrenCommentNumber = c.childrenCommentNumber + :count WHERE c.id = :id")
    int commentCountIncrement(String id, @Param("count") Integer count);
    
    @Query("SELECT c.id FROM CommentNewsModel c WHERE c.news.id = :newsId")
    List<String> findByNewsId(@Param("newsId") String newsId);
    
    @Query("SELECT c.id FROM CommentNewsModel c WHERE c.parentId IN :parentIds")
    List<String> findByParentIds(List<String> parentIds);
}
