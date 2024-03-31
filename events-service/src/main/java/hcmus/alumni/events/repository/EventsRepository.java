package hcmus.alumni.news.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository  extends JpaRepository<NewsRepository, String> {
	long countByIsDeleteEquals(Boolean isDelete);
}
