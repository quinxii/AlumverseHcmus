package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.userservice.dto.profile.IAlumniProfileDto;
import hcmus.alumni.userservice.model.AlumniModel;

public interface AlumniRepository extends JpaRepository<AlumniModel, String> {
	Optional<AlumniModel> findByUserId(String id);

	@Query("SELECT a FROM AlumniModel a WHERE a.userId = :id")
	Optional<IAlumniProfileDto> findAlumniProfileById(@Param("id") String id);
}
