package hcmus.alumni.group.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.group.model.PostGroupModel;

public interface PostGroupRepository extends JpaRepository<PostGroupModel, String> {
	@Query(value = "select count(*) > 0 from post_group where id = :postId and creator = :userId", nativeQuery = true)
	Long isGroupPostOwner(String postId, String userId);
	
	@Query(value = "select count(*) > 0 from post_group pg "
			+ "join `group` g on g.id = pg.group_id and g.privacy = \"PRIVATE\" "
			+ "where pg.id = :postId", nativeQuery = true)
	Long isPrivateByPostId(String postId);
	
	@Query(value = "select count(*) > 0 from group_member gm "
			+ "join post_group pg on pg.id = :postId "
			+ "where pg.group_id = gm.group_id and gm.user_id = :userId and gm.role = :role and gm.is_delete = 0", nativeQuery = true)
	Long hasGroupMemberRoleByPostId(String postId, String userId, String role);
	
	@Query(value = "select count(*) > 0 from group_member gm "
			+ "join post_group pg on pg.id = :postId "
			+ "where pg.group_id = gm.group_id and gm.user_id = :userId and gm.is_delete = 0", nativeQuery = true)
	Long isMemberByPostId(String postId, String userId);
	
	@Query("SELECT DISTINCT new PostGroupModel(p, ip.isDelete, :userId, :canDelete) FROM PostGroupModel p " +
	        "LEFT JOIN p.creator c " +
	        "LEFT JOIN  p.status s " +
	        "LEFT JOIN p.tags t " + 
	        "LEFT JOIN InteractPostGroupModel ip ON p.id = ip.id.postGroupId AND ip.id.creator = :userId " +
	        "WHERE (:title IS NULL OR p.title LIKE %:title%) " +
	        "AND (:tagsId IS NULL OR t.id IN :tagsId) " + 
	        "AND s.id = 2 " +
	        "AND p.groupId = :groupId")
	Page<PostGroupModel> searchPostGroup(
			@Param("groupId") String groupId,
	        @Param("title") String title,
	        @Param("userId") String userId,
	        @Param("tagsId") List<Integer> tagsId,
	        boolean canDelete,
	        Pageable pageable);


    @Query("SELECT DISTINCT new PostGroupModel(p, ip.isDelete, :userId, :canDelete) FROM PostGroupModel p " +
            "JOIN FETCH p.creator " +
            "JOIN FETCH p.status " +
            "LEFT JOIN p.tags " +
            "LEFT JOIN InteractPostGroupModel ip ON p.id = ip.id.postGroupId AND ip.id.creator = :userId " +
            "WHERE p.id = :postId")
    Optional<PostGroupModel> findPostGroupById(@Param("postId") String postId, @Param("userId") String userId, boolean canDelete);
    
    @Transactional
	@Modifying
	@Query("UPDATE PostGroupModel p SET p.childrenCommentNumber = p.childrenCommentNumber + :count WHERE p.id = :id")
	int commentCountIncrement(String id, @Param("count") Integer count);
    
    @Transactional
	@Modifying
	@Query("UPDATE PostGroupModel p SET p.reactionCount = p.reactionCount + :count WHERE p.id = :id")
	int reactionCountIncrement(String id, @Param("count") Integer count);
    
    @Query(value = "select allow_multiple_votes from post_group where id = :postId", nativeQuery = true)
	boolean isAllowMultipleVotes(String postId);

	@Query(value = "select allow_add_options from post_group where id = :postId", nativeQuery = true)
	boolean isAllowAddOptions(String postId);
}
