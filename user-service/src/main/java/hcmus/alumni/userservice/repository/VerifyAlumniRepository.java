package hcmus.alumni.userservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hcmus.alumni.userservice.model.VerifyAlumniModel;

public interface VerifyAlumniRepository extends JpaRepository<VerifyAlumniModel, String> {
	
	@Query(value = "SELECT va.id, user_id, full_name, student_id, beginning_year, email, social_media_link, avatar_url \r\n"
			+ "	FROM verify_alumni va JOIN user u ON va.user_id = u.id;", nativeQuery = true)
	List<Object[]> find();
	
	List<VerifyAlumniModel> findAllByIsDeleteEqualsAndStatusEquals(Boolean isDelete, VerifyAlumniModel.Status status);
	List<VerifyAlumniModel> findAllByIsDeleteEqualsAndStatusNot(Boolean isDelete, VerifyAlumniModel.Status status);

	Optional<VerifyAlumniModel> findByUserIdAndIsDeleteEquals(String userId, Boolean isDelete);
	
	VerifyAlumniModel findByIdAndIsDeleteEquals(String id, Boolean isDelete);
}
