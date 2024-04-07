package hcmus.alumni.news.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import hcmus.alumni.news.dto.INewsDto;
import hcmus.alumni.news.model.NewsModel;

public interface NewsRepository  extends JpaRepository<NewsModel, String> {
	Optional<NewsModel> findById(String id);
	
	Optional<INewsDto> findNewsById(String id);
	
	@Query("SELECT COUNT(n) FROM NewsModel n JOIN n.status s WHERE s.name = :statusName")
	Long getCountByStatus(@Param("statusName") String statusName);
	
	@Query("SELECT n from NewsModel n JOIN n.status s WHERE s.name = \"Chờ\" AND n.publishedAt <= :now")
	List<NewsModel> getScheduledNews(Date now);
}
