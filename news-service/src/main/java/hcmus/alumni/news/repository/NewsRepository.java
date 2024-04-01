package hcmus.alumni.news.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.news.model.NewsModel;

public interface NewsRepository  extends JpaRepository<NewsModel, String> {
	Optional<NewsModel> findById(String id);
	@Query("SELECT COUNT(n) FROM NewsModel n JOIN n.status s WHERE s.name = :statusName")
	Long getCountByStatus(@Param("statusName") String statusName);
}
