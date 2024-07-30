package hcmus.alumni.group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.CommentPostGroupModel;
import hcmus.alumni.group.dto.response.ICommentPostGroupDto;

public interface CommentPostGroupRepository extends JpaRepository<CommentPostGroupModel, String> {
	@Query(value = "select count(*) > 0 from comment_post_group where id = :commentId and creator = :userId", nativeQuery = true)
	Long isCommentOwner(String commentId, String userId);
	
	@Query(value = "select count(*) > 0 from group_member gm "
			+ "join comment_post_group cpg on cpg.id = :commentId "
			+ "join post_group pg on pg.id = cpg.post_group_id "
			+ "where pg.group_id = gm.group_id and gm.user_id = :userId and gm.role = :role and gm.is_delete = 0", nativeQuery = true)
	Long hasGroupMemberRoleByCommentId(String commentId, String userId, String role);
	
	@Query("SELECT new CommentPostGroupModel(c, :userId, :canDelete) FROM CommentPostGroupModel c " +
	        "WHERE c.postGroup.id = :postId AND c.id = :commentId AND c.isDelete = false")
	Optional<ICommentPostGroupDto> getComment(String postId, String commentId, String userId, boolean canDelete);
	
	@Query("SELECT new CommentPostGroupModel(c, :userId, :canDelete) FROM CommentPostGroupModel c " +
            "WHERE c.postGroup.id = :postGroupId AND c.isDelete = false AND c.parentId IS NULL")
    Page<ICommentPostGroupDto> getComments(String postGroupId, String userId, boolean canDelete, Pageable pageable);

    @Query("SELECT new CommentPostGroupModel(c, :userId, :canDelete) FROM CommentPostGroupModel c " +
            "WHERE c.isDelete = false AND c.parentId = :commentId")
    Page<ICommentPostGroupDto> getChildrenComment(String commentId, String userId, boolean canDelete, Pageable pageable);

    @Query("SELECT c FROM CommentPostGroupModel c WHERE c.isDelete = false AND c.parentId = :commentId")
    List<CommentPostGroupModel> getChildrenComment(String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.content = :content WHERE c.id = :commentId AND c.creator.id = :creator")
    int updateComment(@Param("commentId") String commentId, @Param("creator") String creator,
            @Param("content") String content);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.isDelete = true WHERE c.id = :commentId")
    int deleteComment(@Param("commentId") String commentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.isDelete = true WHERE c.parentId = :parentId AND c.isDelete = false")
    int deleteChildrenComment(@Param("parentId") String parentId);

    @Transactional
    @Modifying
    @Query("UPDATE CommentPostGroupModel c SET c.childrenCommentNumber = c.childrenCommentNumber + :count WHERE c.id = :id")
    int commentCountIncrement(String id, @Param("count") Integer count);
    
	@Query("SELECT c.id FROM CommentPostGroupModel c JOIN c.postGroup p WHERE p.groupId = :groupId")
	List<String> findByGroupId(@Param("groupId") String groupId);
	
	@Query("SELECT c.id FROM CommentPostGroupModel c WHERE c.postGroup.id = :postGroupId")
	List<String> findByPostGroupId(@Param("postGroupId") String postGroupId);
	
	@Query("SELECT c.id FROM CommentPostGroupModel c WHERE c.parentId IN :parentIds")
	List<String> findByParentIds(List<String> parentIds);
}
