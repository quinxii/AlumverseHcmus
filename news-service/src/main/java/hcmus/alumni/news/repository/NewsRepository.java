package hcmus.alumni.news.repository;

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

import hcmus.alumni.news.dto.INewsDto;
import hcmus.alumni.news.model.NewsModel;

public interface NewsRepository extends JpaRepository<NewsModel, String> {
	Optional<NewsModel> findById(String id);

	Optional<INewsDto> findNewsById(String id);

	@Query("SELECT DISTINCT n " +
			"FROM NewsModel n " +
			"LEFT JOIN n.status s " +
			"LEFT JOIN n.faculty f " +
			"WHERE (:statusId IS NULL OR s.id = :statusId) " +
			"AND (:facultyId IS NULL OR f.id = :facultyId) " +
			"AND (:tagsId IS NULL OR element(n.tags).id IN :tagsId) " +
			"AND s.id != 4 " +
			"AND n.title LIKE %:title%")
	Page<INewsDto> searchNews(String title, Integer facultyId, List<Integer> tagsId, Integer statusId, Pageable pageable);

	@Query("SELECT n FROM NewsModel n JOIN n.status s WHERE s.id = 2")
	Page<INewsDto> getMostViewdNews(Pageable pageable);

	@Query("SELECT n FROM NewsModel n JOIN n.status s WHERE s.id = 2 AND n.publishedAt >= :startDate AND n.publishedAt <= :endDate")
	Page<INewsDto> getHotNews(Date startDate, Date endDate, Pageable pageable);

	@Query("SELECT COUNT(n) FROM NewsModel n JOIN n.status s WHERE s.id = :statusId")
	Long getCountByStatusId(@Param("statusId") Integer statusId);

	@Query("SELECT COUNT(n) FROM NewsModel n JOIN n.status s WHERE s.id != 4")
	Long getCountByNotDelete();

	@Query("SELECT n from NewsModel n JOIN n.status s WHERE s.id = 1 AND n.publishedAt <= :now")
	List<NewsModel> getScheduledNews(Date now);

	@Transactional
	@Modifying
	@Query("UPDATE NewsModel n SET n.views = n.views + 1 WHERE n.id = :id")
	int viewsIncrement(String id);

	@Transactional
	@Modifying
	@Query("UPDATE NewsModel n SET n.childrenCommentNumber = n.childrenCommentNumber + :count WHERE n.id = :id")
	int commentCountIncrement(String id, @Param("count") Integer count);
}