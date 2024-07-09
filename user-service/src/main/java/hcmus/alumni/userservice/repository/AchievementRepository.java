package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.IAchievementDto;
import hcmus.alumni.userservice.model.AchievementModel;

public interface AchievementRepository extends JpaRepository<AchievementModel, String> {
	@Query("SELECT a FROM AchievementModel a WHERE a.userId = :id AND a.isDelete = false")
	List<AchievementModel> findByUserId(String id);

	Optional<IAchievementDto> findByAchievementId(String achievementId);

	@Query("SELECT a FROM AchievementModel a WHERE a.userId = :userId AND a.achievementName = :achievementName AND a.achievementType = :achievementType")
	Optional<AchievementModel> findByUserIdAndAchievementNameAndAchievementType(String userId, String achievementName, String achievementType);

	Optional<AchievementModel> findByUserIdAndAchievementId(String id, String achievementId);

}
