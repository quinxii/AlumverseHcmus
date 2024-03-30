package hcmus.alumni.halloffame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HallOfFameRepository extends JpaRepository<HallOfFameRepository, String> {
	long countByIsDeleteEquals(Boolean isDelete);
}
