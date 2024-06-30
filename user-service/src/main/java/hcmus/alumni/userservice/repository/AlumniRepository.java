package hcmus.alumni.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hcmus.alumni.userservice.dto.IAlumniProfileDto;
import hcmus.alumni.userservice.model.AlumniModel;

public interface AlumniRepository extends JpaRepository<AlumniModel, String> {
	Optional<AlumniModel> findByUserId(String id);

	@Query("SELECT u.id AS id, u.avatarUrl AS avatarUrl, u.coverUrl AS coverUrl, u.fullName AS fullName, "
			+ "u.faculty AS faculty, u.sex AS sex, u.dob AS dob, u.socialMediaLink AS socialMediaLink, "
			+ "u.email AS email, u.phone AS phone, u.aboutMe AS aboutMe, "
			+ "a.alumClass AS alumClass, a.graduationYear AS graduationYear "
			+ "FROM UserModel u LEFT JOIN AlumniModel a ON u.id = a.userId WHERE u.id = :id")
	Optional<IAlumniProfileDto> findAlumniProfileById(@Param("id") String id);
}
