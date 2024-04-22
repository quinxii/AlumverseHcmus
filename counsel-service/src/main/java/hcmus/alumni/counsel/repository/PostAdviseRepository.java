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

import hcmus.alumni.counsel.dto.IPostAdviseDto;
import hcmus.alumni.counsel.model.PostAdviseModel;

public interface PostAdviseRepository  extends JpaRepository<PostAdviseModel, String> {
	Optional<PostAdviseModel> findById(String id);
	
	Optional<IPostAdviseDto> findNewsById(String id);
	
	@Query("SELECT n FROM PostAdviseModel n JOIN n.status s WHERE s.id != 4 AND n.title like %:title%")
	Page<IPostAdviseDto> searchNews(String title, Pageable pageable);
	
	@Query("SELECT n FROM PostAdviseModel n JOIN n.status s WHERE s.id = :statusId AND n.title like %:title%")
	Page<IPostAdviseDto> searchNewsByStatus(String title, Integer statusId, Pageable pageable);

	@Query("SELECT n FROM PostAdviseModel n JOIN n.status s WHERE s.id = 2")
	Page<IPostAdviseDto> getMostViewdNews(Pageable pageable);

	@Query("SELECT n FROM PostAdviseModel n JOIN n.status s WHERE s.id = 2 AND n.publishedAt >= :startDate AND n.publishedAt <= :endDate")
	Page<IPostAdviseDto> getHotNews(Date startDate, Date endDate, Pageable pageable);
	
	@Query("SELECT COUNT(n) FROM PostAdviseModel n JOIN n.status s WHERE s.id = :statusId")
	Long getCountByStatusId(@Param("statusId") Integer statusId);
	@Query("SELECT COUNT(n) FROM PostAdviseModel n JOIN n.status s WHERE s.id != 4")
	Long getCountByNotDelete();
	
	@Query("SELECT n from PostAdviseModel n JOIN n.status s WHERE s.id = 1 AND n.publishedAt <= :now")
	List<PostAdviseModel> getScheduledNews(Date now);

	@Transactional
	@Modifying
	@Query("UPDATE PostAdviseModel n SET n.childrenCommentNumber = n.childrenCommentNumber + :count WHERE n.id = :id")
	int commentCountIncrement(String id,@Param("count") Integer count);
}