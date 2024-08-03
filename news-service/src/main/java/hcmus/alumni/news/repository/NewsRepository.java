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
import hcmus.alumni.news.dto.INewsListDto;
import hcmus.alumni.news.model.NewsModel;

public interface NewsRepository extends JpaRepository<NewsModel, String> {
	Optional<NewsModel> findById(String id);

	Optional<INewsDto> findNewsById(String id);

	@Query(value = "SELECT n " +
			"FROM NewsModel n " +
			"LEFT JOIN n.status s " +
			"LEFT JOIN n.faculty f " +
			"LEFT JOIN FETCH n.tags t " +
			"WHERE (:statusId IS NULL OR s.id = :statusId) " +
			"AND (:facultyId IS NULL OR f.id = :facultyId) " +
			"AND (:tagNames IS NULL OR t.name IN :tagNames) " +
			"AND s.id != 4 " +
			"AND (:title IS NULL OR n.title LIKE %:title%)", countQuery = "SELECT COUNT(DISTINCT n) " +
					"FROM NewsModel n " +
					"LEFT JOIN n.status s " +
					"LEFT JOIN n.faculty f " +
					"LEFT JOIN n.tags t " +
					"WHERE (:statusId IS NULL OR s.id = :statusId) " +
					"AND (:facultyId IS NULL OR f.id = :facultyId) " +
					"AND (:tagNames IS NULL OR t.name IN :tagNames) " +
					"AND s.id != 4 " +
					"AND (:title IS NULL OR n.title LIKE %:title%)")
	Page<INewsListDto> searchNews(String title, Integer facultyId, List<String> tagNames, Integer statusId,
			Pageable pageable);

	@Query("SELECT DISTINCT n " +
			"FROM NewsModel n " +
			"LEFT JOIN n.faculty f " +
			"LEFT JOIN n.tags t " +
			"WHERE ((f.id = :facultyId) " +
			"OR (t.id IN :tagsId)) " +
			"AND n.status.id = 2 " +
			"AND n.id != :originalNewsId")
	Page<INewsListDto> getRelatedNews(String originalNewsId, Integer facultyId, List<Long> tagsId, Pageable pageable);

	@Query("SELECT n FROM NewsModel n JOIN n.status s WHERE s.id = 2")
	Page<INewsListDto> getMostViewdNews(Pageable pageable);

	@Query("SELECT n FROM NewsModel n JOIN n.status s WHERE s.id = 2 AND n.publishedAt >= :startDate AND n.publishedAt <= :endDate")
	Page<INewsListDto> getHotNews(Date startDate, Date endDate, Pageable pageable);

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

	@Query(value = "select distinct p.name from role_permission rp " +
			"join permission p on p.id = rp.permission_id and p.is_delete = false " +
			"join role r on r.id = rp.role_id and r.is_delete = false " +
			"where r.id in (select role_id from user_role where user_id = :userId) and p.name like :domain% and rp.is_delete = false;", nativeQuery = true)
	List<String> getPermissions(String userId, String domain);
}