package hcmus.alumni.halloffame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.halloffame.model.HallOfFameModel;


public interface HallOfFameRepository extends JpaRepository<HallOfFameModel, String> {
	Optional<HallOfFameModel> findById(String id);
	@Query("SELECT COUNT(n) FROM HallOfFameModel n JOIN n.status s WHERE s.name = :statusName")
	Long getCountByStatus(@Param("statusName") String statusName);
}