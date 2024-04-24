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

import hcmus.alumni.group.model.PostGroupModel;
import hcmus.alumni.group.dto.IPostGroupDto;

public interface PostGroupRepository extends JpaRepository<PostGroupModel, String> {        
	@Query("SELECT p FROM PostGroupModel p " +
	        "LEFT JOIN p.creator c " +
	        "LEFT JOIN  p.status s " +
	        "LEFT JOIN FETCH p.tags t " + 
	        "WHERE (:title IS NULL OR p.title LIKE %:title%) " +
	        "AND (:tagsId IS NULL OR t.id IN :tagsId) " + 
	        "AND (:statusId IS NULL OR s.id = :statusId)")
	Page<IPostGroupDto> searchGroupPosts(
	        @Param("title") String title,
	        @Param("tagsId") List<Integer> tagsId,
	        @Param("statusId") Integer statusId,
	        Pageable pageable);


    @Query("SELECT p FROM PostGroupModel p " +
            "JOIN FETCH p.creator " +
            "JOIN FETCH p.status " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id = :postId")
    Optional<IPostGroupDto> findPostById(@Param("postId") String postId);
    
    @Transactional
	@Modifying
	@Query("UPDATE PostGroupModel p SET p.childrenCommentNumber = p.childrenCommentNumber + :count WHERE p.id = :id")
	int commentCountIncrement(String id, @Param("count") Integer count);
}
