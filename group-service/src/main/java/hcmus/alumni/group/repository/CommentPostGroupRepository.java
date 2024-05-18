package hcmus.alumni.group.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.CommentPostGroupModel;
import hcmus.alumni.group.dto.ICommentPostGroupDto;

public interface CommentPostGroupRepository extends JpaRepository<CommentPostGroupModel, String> {        
	@Query("SELECT new CommentPostGroupModel(c, :userId) FROM CommentPostGroupModel c " +
            "WHERE c.postGroup.id = :postGroupId AND c.isDelete = false AND c.parentId IS NULL")
    Page<ICommentPostGroupDto> getComments(String postGroupId, String userId, Pageable pageable);

    @Query("SELECT new CommentPostGroupModel(c, :userId) FROM CommentPostGroupModel c " +
            "WHERE c.isDelete = false AND c.parentId = :commentId")
    Page<ICommentPostGroupDto> getChildrenComment(String commentId, String userId, Pageable pageable);

    @Query("SELECT c FROM CommentPostGroupModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    List<CommentPostGroupModel> getChildrenComment(String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.content = :content WHERE c.id = :commentId AND c.creator.id = :creator")
    int updateComment(@Param("commentId") String commentId, @Param("creator") String creator,
            @Param("content") String content);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.isDelete = true WHERE c.id = :commentId AND c.creator.id = :creator")
    int deleteComment(@Param("commentId") String commentId, @Param("creator") String creator);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.isDelete = true WHERE c.parentId = :parentId AND c.isDelete = false")
    int deleteChildrenComment(@Param("parentId") String parentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.childrenCommentNumber = c.childrenCommentNumber + :count WHERE c.id = :id")
    int commentCountIncrement(String id, @Param("count") Integer count);
}
