package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.IAchievementDto;
import hcmus.alumni.userservice.model.AchievementModel;

public interface AchievementRepository extends JpaRepository<AchievementModel, String> {
	@Query("SELECT a FROM AchievementModel a WHERE a.isDelete = false")
	List<AchievementModel> findByUserId(String id);

	Optional<IAchievementDto> findByAchievementId(String achievementId);

	@Query("SELECT a FROM AchievementModel a WHERE a.userId = :userId AND a.name = :name AND a.type = :type")
	Optional<AchievementModel> findByUserIdAndNameAndType(String id, String name, String type);

	Optional<AchievementModel> findByUserIdAndAchievementId(String id, String achievementId);

}
