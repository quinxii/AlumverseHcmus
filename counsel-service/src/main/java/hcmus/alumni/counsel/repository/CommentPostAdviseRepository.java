package hcmus.alumni.counsel.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.counsel.dto.ICommentPostAdviseDto;
import hcmus.alumni.counsel.model.CommentPostAdviseModel;


public interface CommentPostAdviseRepository extends JpaRepository<CommentPostAdviseModel, String> {

    @Query("SELECT c FROM CommentPostAdviseModel c WHERE c.news.id = :newsId AND c.isDelete = false AND c.parentId IS NULL")
    Page<ICommentPostAdviseDto> getComments(String newsId, Pageable pageable);

    @Query("SELECT c FROM CommentPostAdviseModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    Page<ICommentPostAdviseDto> getChildrenComment(String commentId, Pageable pageable);

    @Query("SELECT c FROM CommentPostAdviseModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    List<CommentPostAdviseModel> getChildrenComment(String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.content = :content WHERE c.id = :commentId AND c.creator.id = :creator")
    int updateComment(@Param("commentId") String commentId, @Param("creator") String creator,
            @Param("content") String content);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.isDelete = true WHERE c.id = :commentId AND c.creator.id = :creator")
    int deleteComment(@Param("commentId") String commentId, @Param("creator") String creator);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostAdviseModel c SET c.isDelete = true WHERE c.parentId = :parentId")
    int deleteChildrenComment(@Param("parentId") String parentId);
}
