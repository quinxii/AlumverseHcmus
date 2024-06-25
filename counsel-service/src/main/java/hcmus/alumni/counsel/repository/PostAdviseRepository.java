package hcmus.alumni.counsel.repository;

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

import hcmus.alumni.counsel.model.PostAdviseModel;

public interface PostAdviseRepository extends JpaRepository<PostAdviseModel, String> {
	@Query(value = "select count(*) > 0 from post_advise where " +
			"id = :postId and creator = :userId", nativeQuery = true)
	Long isPostOwner(String postId, String userId);

	Optional<PostAdviseModel> findById(String id);

	@Query("SELECT pa FROM PostAdviseModel pa JOIN pa.status s WHERE s.id != 4 AND pa.id = :id AND pa.creator.id = :creatorId")
	Optional<PostAdviseModel> findByIdAndCreator(@Param("id") String id, @Param("creatorId") String creatorId);

	@Query("SELECT DISTINCT new PostAdviseModel(pa, ipa.isDelete, :userId, :canDelete) " +
			"FROM PostAdviseModel pa " +
			"LEFT JOIN InteractPostAdviseModel ipa ON pa.id = ipa.id.postAdviseId AND ipa.id.creator = :userId " +
			"JOIN pa.status s WHERE s.id = 2 AND pa.id = :id")
	Optional<PostAdviseModel> findPostAdviseById(String id, String userId, boolean canDelete);

	@Query("SELECT DISTINCT new PostAdviseModel(pa, ipa.isDelete, :userId, :canDelete) " +
			"FROM PostAdviseModel pa " +
			"JOIN pa.status s " +
			"LEFT JOIN pa.tags t " +
			"LEFT JOIN InteractPostAdviseModel ipa ON pa.id = ipa.id.postAdviseId AND ipa.id.creator = :userId " +
			"WHERE (:tagNames IS NULL OR t.name IN :tagNames) " +
			"AND s.id = 2 " +
			"AND (:title IS NULL OR pa.title like %:title%)")
	Page<PostAdviseModel> searchPostAdvise(String title, String userId, boolean canDelete, List<String> tagNames,
			Pageable pageable);

	@Query("SELECT DISTINCT new PostAdviseModel(pa, ipa.isDelete, :reqUserId, :canDelete) " +
			"FROM PostAdviseModel pa " +
			"JOIN pa.status s " +
			"LEFT JOIN pa.tags t " +
			"LEFT JOIN InteractPostAdviseModel ipa ON pa.id = ipa.id.postAdviseId AND ipa.id.creator = :reqUserId " +
			"WHERE (:tagNames IS NULL OR t.name IN :tagNames) " +
			"AND s.id = 2 " +
			"AND (:title IS NULL OR pa.title like %:title%) " +
			"AND pa.creator.id = :ofUserId")
	Page<PostAdviseModel> searchPostAdviseOfUser(String ofUserId, String title, String reqUserId, boolean canDelete,
			List<String> tagNames,
			Pageable pageable);

	@Query("SELECT COUNT(pa) FROM PostAdviseModel pa JOIN pa.status s WHERE s.id = :statusId")
	Long getCountByStatusId(@Param("statusId") Integer statusId);

	@Query("SELECT COUNT(pa) FROM PostAdviseModel pa JOIN pa.status s WHERE s.id != 4")
	Long getCountByNotDelete();

	@Query("SELECT pa from PostAdviseModel pa JOIN pa.status s WHERE s.id = 1 AND pa.publishedAt <= :now")
	List<PostAdviseModel> getScheduledNews(Date now);

	@Transactional
	@Modifying
	@Query("UPDATE PostAdviseModel pa SET pa.childrenCommentNumber = pa.childrenCommentNumber + :count WHERE pa.id = :id")
	int commentCountIncrement(String id, @Param("count") Integer count);

	@Transactional
	@Modifying
	@Query("UPDATE PostAdviseModel pa SET pa.reactionCount = pa.reactionCount + :count WHERE pa.id = :id")
	int reactionCountIncrement(String id, @Param("count") Integer count);

	@Query(value = "select allow_multiple_votes from post_advise " +
			"where id = :postId", nativeQuery = true)
	boolean isAllowMultipleVotes(String postId);

	@Query(value = "select allow_add_options from post_advise " +
			"where id = :postId", nativeQuery = true)
	boolean isAllowAddOptions(String postId);

	@Query(value = "select distinct p.name from role_permission rp " +
			"join role r on r.id = rp.role_id and r.is_delete = false " +
			"join permission p on p.id = rp.permission_id and p.is_delete = false " +
			"where r.name in :role and p.name like :domain% and rp.is_delete = false", nativeQuery = true)
	List<String> getPermissions(List<String> role, String domain);
}