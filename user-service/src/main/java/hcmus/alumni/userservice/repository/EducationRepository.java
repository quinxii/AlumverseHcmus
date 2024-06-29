package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.dto.IEducationDto;
import hcmus.alumni.userservice.model.EducationModel;

public interface EducationRepository extends JpaRepository<EducationModel, String> {
	@Query("SELECT e FROM EducationModel e WHERE e.isDelete = false")
	List<EducationModel> findByUserId(String id);

	Optional<EducationModel> findByUserIdAndSchoolName(String userId, String schoolName);

	Optional<IEducationDto> findByEducationId(String id);
	
	@Query("SELECT e FROM EducationModel e WHERE e.userId = :userId AND e.schoolName = :schoolName AND e.degree = :degree")
	Optional<EducationModel> findByUserIdAndSchoolNameAndDegree(String userId, String schoolName, String degree);

	Optional<EducationModel> findByUserIdAndEducationId(String id, String educationId);

}
